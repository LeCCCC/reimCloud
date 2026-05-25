package org.example.reimcloud.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.example.reimcloud.common.BillStatus;
import org.example.reimcloud.common.BusinessException;
import org.example.reimcloud.common.Ids;
import org.example.reimcloud.dto.AllocationDTO;
import org.example.reimcloud.dto.SubsidyCalendarSaveDTO;
import org.example.reimcloud.dto.TravelReimbursementQueryDTO;
import org.example.reimcloud.dto.TravelReimbursementSaveDTO;
import org.example.reimcloud.dto.TripDTO;
import org.example.reimcloud.entity.FkReimMain;
import org.example.reimcloud.entity.ReimAllocation;
import org.example.reimcloud.entity.ReimItinerary;
import org.example.reimcloud.entity.ReimSubsidy;
import org.example.reimcloud.entity.SubsidyCalendar;
import org.example.reimcloud.mapper.BaseDataMapper;
import org.example.reimcloud.mapper.FkReimMainMapper;
import org.example.reimcloud.mapper.ReimAllocationMapper;
import org.example.reimcloud.mapper.ReimItineraryMapper;
import org.example.reimcloud.mapper.ReimSubsidyMapper;
import org.example.reimcloud.mapper.SubsidyCalendarMapper;
import org.example.reimcloud.service.TravelReimbursementService;
import org.example.reimcloud.vo.AllocationVO;
import org.example.reimcloud.vo.CostSummaryVO;
import org.example.reimcloud.vo.PageVO;
import org.example.reimcloud.vo.SubmitResult;
import org.example.reimcloud.vo.SubsidyCalendarItemVO;
import org.example.reimcloud.vo.SubsidyCalendarVO;
import org.example.reimcloud.vo.SubsidyVO;
import org.example.reimcloud.vo.TravelReimbursementCreateResult;
import org.example.reimcloud.vo.TravelReimbursementDetailVO;
import org.example.reimcloud.vo.TravelReimbursementListVO;
import org.example.reimcloud.vo.TravelReimbursementUpdateResult;
import org.example.reimcloud.vo.TripVO;
import org.example.reimcloud.vo.VoidResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelReimbursementServiceImpl implements TravelReimbursementService {

    private static final BigDecimal ZERO = bd("0");
    private static final BigDecimal TRANSPORT_STANDARD = bd("40");
    private static final BigDecimal PHONE_STANDARD = bd("40");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FkReimMainMapper mainMapper;
    private final ReimItineraryMapper itineraryMapper;
    private final ReimSubsidyMapper subsidyMapper;
    private final SubsidyCalendarMapper calendarMapper;
    private final ReimAllocationMapper allocationMapper;
    private final BaseDataMapper baseDataMapper;

    // ========== 1.1 列表 ==========
    @Override
    public PageVO<TravelReimbursementListVO> listTravelReimbursements(TravelReimbursementQueryDTO query) {
        int current = query.getCurrent() != null ? query.getCurrent() : 1;
        int size = query.getSize() != null ? query.getSize() : 10;
        int offset = (current - 1) * size;

        long total = mainMapper.countByQuery(query);
        List<FkReimMain> list = mainMapper.selectPage(query, offset);
        List<TravelReimbursementListVO> records = list.stream().map(this::toListVO).toList();

        long pages = (total + size - 1) / size;
        return PageVO.<TravelReimbursementListVO>builder()
                .total(total).pages(pages).current((long) current).size((long) size).records(records).build();
    }

    // ========== 1.2 新增 ==========
    @Override
    @Transactional
    public TravelReimbursementCreateResult create(TravelReimbursementSaveDTO dto) {
        validateBase(dto);
        validateTrips(dto.getTripList(), false);

        LocalDateTime now = LocalDateTime.now();
        String id = Ids.newId();
        String billNo = nextBillNo();
        FkReimMain main = buildMain(id, billNo, BillStatus.DRAFT, now, now, dto);

        DetailData detail = buildDetail(id, dto);
        applyTotals(main, detail);
        mainMapper.insert(main);
        insertDetail(id, detail, dto);

        return TravelReimbursementCreateResult.builder().id(id).billNo(billNo).billStatus(BillStatus.DRAFT).build();
    }

    // ========== 1.3 详情 ==========
    @Override
    public TravelReimbursementDetailVO getDetail(String id) {
        FkReimMain main = requireMain(id);
        List<ReimItinerary> itineraries = itineraryMapper.selectByMainId(id);
        List<ReimSubsidy> subsidies = subsidyMapper.selectByMainId(id);
        List<ReimAllocation> allocations = allocationMapper.selectByMainId(id);

        Map<String, String> sidByTid = itineraries.stream()
                .collect(Collectors.toMap(ReimItinerary::getId, ReimItinerary::getSubsidyId, (a, b) -> a));

        return TravelReimbursementDetailVO.builder()
                .id(main.getId()).billNo(main.getBillNo()).billStatus(main.getBillStatus())
                .creationTime(fmt(main.getCreationTime()))
                .reimbursementTitle(main.getReimbursementTitle()).businessTripReason(main.getBusinessTripReason())
                .reimburserId(main.getReimburserId()).reimburserNo(main.getReimburserNo()).reimburserName(main.getReimburserName())
                .reimDepartmentId(main.getReimDepartmentId()).reimDepartmentName(main.getReimDepartmentName())
                .reimCompanyId(main.getReimCompanyId()).reimCompanyName(main.getReimCompanyName())
                .businessTypeId(main.getBusinessTypeId()).businessTypeName(main.getBusinessTypeName())
                .tripList(itineraries.stream().map(it -> toTripVO(it, sidByTid.get(it.getId()))).toList())
                .subsidyList(subsidies.stream().map(this::toSubsidyVO).toList())
                .costSummary(toCostSummary(main))
                .allocationList(allocations.stream().map(this::toAllocationVO).toList())
                .remarks(main.getRemarks()).build();
    }

    // ========== 1.4 修改 ==========
    @Override
    @Transactional
    public TravelReimbursementUpdateResult update(String id, TravelReimbursementSaveDTO dto) {
        FkReimMain old = requireMain(id);
        if (!BillStatus.DRAFT.equals(old.getBillStatus())) {
            throw new BusinessException("只有草稿状态的单据允许修改");
        }
        validateBase(dto);
        validateTrips(dto.getTripList(), false);

        LocalDateTime now = LocalDateTime.now();
        FkReimMain main = buildMain(id, old.getBillNo(), old.getBillStatus(), old.getCreationTime(), now, dto);
        DetailData detail = buildDetail(id, dto);
        applyTotals(main, detail);

        deleteDetail(id);
        int rows = mainMapper.updateDraft(main);
        if (rows == 0) {
            throw new BusinessException("该单据状态已变更，请刷新后重试");
        }
        insertDetail(id, detail, dto);

        return TravelReimbursementUpdateResult.builder().id(id).billStatus(BillStatus.DRAFT).updateTime(fmt(now)).build();
    }

    // ========== 1.5 提交 ==========
    @Override
    @Transactional
    public SubmitResult submit(String id) {
        FkReimMain main = requireMain(id);
        if (!BillStatus.DRAFT.equals(main.getBillStatus())) {
            throw new BusinessException("只有草稿状态的单据允许提交");
        }
        if (itineraryMapper.selectByMainId(id).isEmpty()) {
            throw new BusinessException("提交时至少需要一条补录行程");
        }

        LocalDateTime now = LocalDateTime.now();
        main.setBillStatus(BillStatus.COMPLETED);
        main.setUpdateTime(now);
        int rows = mainMapper.updateStatus(main);
        if (rows == 0) {
            throw new BusinessException("该单据状态已变更，请刷新后重试");
        }

        return SubmitResult.builder().id(id).billNo(main.getBillNo()).billStatus(BillStatus.COMPLETED).submitTime(fmt(now)).build();
    }

    // ========== 1.6 作废 ==========
    @Override
    @Transactional
    public VoidResultVO voidReimbursement(String id) {
        FkReimMain main = requireMain(id);
        if (BillStatus.VOIDED.equals(main.getBillStatus())) {
            throw new BusinessException("该报销单已作废，不可重复作废");
        }
        int rows = mainMapper.updateBillStatus(id, BillStatus.VOIDED);
        if (rows == 0) {
            throw new BusinessException("该单据状态已变更，请刷新后重试");
        }
        return VoidResultVO.builder().id(id).billStatus(BillStatus.VOIDED).build();
    }

    // ========== 2.1 新增行程 ==========
    @Override
    @Transactional
    public TripVO createTrip(String id, TripDTO dto) {
        requireMain(id);
        validateTrip(dto);
        checkTripOverlap(id, null, dto);

        String tripId = Ids.newId();
        String subsidyId = Ids.newId();
        ReimItinerary itinerary = buildItinerary(tripId, id, subsidyId, dto);
        itineraryMapper.insert(itinerary);

        ReimSubsidy subsidy = buildSubsidy(subsidyId, id, dto);
        subsidyMapper.insert(subsidy);
        buildAndInsertCalendars(subsidyId, dto);

        recalcMain(id);
        return toTripVO(itinerary, subsidyId);
    }

    // ========== 2.2 修改行程 ==========
    @Override
    @Transactional
    public TripVO updateTrip(String id, String tripId, TripDTO dto) {
        requireMain(id);
        ReimItinerary old = itineraryMapper.selectById(tripId);
        if (old == null) throw new BusinessException("补录行程不存在");
        validateTrip(dto);
        checkTripOverlap(id, tripId, dto);

        String subsidyId = old.getSubsidyId();

        // 删除该行程关联的补助日历和补助信息
        calendarMapper.deleteBySubsidyId(subsidyId);
        subsidyMapper.deleteById(subsidyId);

        // 重建补助日历
        ReimSubsidy subsidy = buildSubsidy(subsidyId, id, dto);
        subsidyMapper.insert(subsidy);
        buildAndInsertCalendars(subsidyId, dto);

        // 更新行程
        ReimItinerary itinerary = buildItinerary(tripId, id, subsidyId, dto);
        itineraryMapper.update(itinerary);

        recalcMain(id);
        return toTripVO(itinerary, subsidyId);
    }

    // ========== 2.3 删除行程 ==========
    @Override
    @Transactional
    public void deleteTrip(String id, String tripId) {
        requireMain(id);
        ReimItinerary itinerary = itineraryMapper.selectById(tripId);
        if (itinerary == null) throw new BusinessException("补录行程不存在");
        String subsidyId = itinerary.getSubsidyId();
        itineraryMapper.deleteById(tripId);
        calendarMapper.deleteBySubsidyId(subsidyId);
        subsidyMapper.deleteById(subsidyId);
        recalcMain(id);
    }

    // ========== 3.1 查询补助日历 ==========
    @Override
    public SubsidyCalendarVO getSubsidyCalendar(String id, String subsidyId) {
        FkReimMain main = requireMain(id);
        ReimSubsidy subsidy = subsidyMapper.selectById(subsidyId);
        if (subsidy == null) throw new BusinessException("补助信息不存在");
        List<SubsidyCalendar> calendars = calendarMapper.selectBySubsidyId(subsidyId);

        BigDecimal subsidyAmount = calendars.stream().map(c -> selectedAmount(c)).reduce(ZERO, BigDecimal::add);
        BigDecimal standardAmount = calendars.stream().map(c -> selectedStandard(c)).reduce(ZERO, BigDecimal::add);

        return SubsidyCalendarVO.builder()
                .subsidyId(subsidyId).businessTypeName(main.getBusinessTypeName())
                .startDate(fmt(subsidy.getDepartureDate())).endDate(fmt(subsidy.getArrivalDate()))
                .days(subsidy.getSubsidyDays())
                .route(subsidy.getDepartureCity() + "-" + subsidy.getArrivingCity())
                .subsidyAmount(scale(subsidyAmount)).standardAmount(scale(standardAmount))
                .calendarList(calendars.stream().map(this::toCalendarItemVO).toList()).build();
    }

    // ========== 3.2 保存补助日历 ==========
    @Override
    @Transactional
    public void saveSubsidyCalendar(String id, String subsidyId, List<SubsidyCalendarSaveDTO> calendarList) {
        requireMain(id);
        for (SubsidyCalendarSaveDTO dto : calendarList) {
            SubsidyCalendar cal = calendarMapper.selectBySubsidyId(subsidyId).stream()
                    .filter(c -> Objects.equals(c.getId(), dto.getCalendarId())).findFirst()
                    .orElseThrow(() -> new BusinessException("补助日历明细不存在: " + dto.getCalendarId()));

            if (Boolean.TRUE.equals(dto.getMealSelected())) {
                if (dto.getMealAmount() == null || dto.getMealAmount().compareTo(BigDecimal.ZERO) <= 0)
                    throw new BusinessException("餐费申请金额必须为正数");
                if (dto.getMealAmount().compareTo(cal.getStandardMealExpensesAmount()) > 0)
                    throw new BusinessException("餐费申请金额不能大于标准金额");
                cal.setMealSelected("1");
                cal.setMealExpensesAmount(dto.getMealAmount());
            } else {
                cal.setMealSelected("0");
                cal.setMealExpensesAmount(ZERO);
            }

            if (Boolean.TRUE.equals(dto.getTransportSelected())) {
                if (dto.getTransportAmount() == null || dto.getTransportAmount().compareTo(BigDecimal.ZERO) <= 0)
                    throw new BusinessException("交通申请金额必须为正数");
                if (dto.getTransportAmount().compareTo(cal.getStandardTrafficAmount()) > 0)
                    throw new BusinessException("交通申请金额不能大于标准金额");
                cal.setTrafficSelected("1");
                cal.setTrafficAmount(dto.getTransportAmount());
            } else {
                cal.setTrafficSelected("0");
                cal.setTrafficAmount(ZERO);
            }

            if (Boolean.TRUE.equals(dto.getPhoneSelected())) {
                if (dto.getPhoneAmount() == null || dto.getPhoneAmount().compareTo(BigDecimal.ZERO) <= 0)
                    throw new BusinessException("通讯申请金额必须为正数");
                if (dto.getPhoneAmount().compareTo(cal.getStandardCommunicationAmount()) > 0)
                    throw new BusinessException("通讯申请金额不能大于标准金额");
                cal.setCommunicationSelected("1");
                cal.setCommunicationAmount(dto.getPhoneAmount());
            } else {
                cal.setCommunicationSelected("0");
                cal.setCommunicationAmount(ZERO);
            }
            calendarMapper.update(cal);
        }
        recalcMainFromCalendars(id);
    }

    // ========== 4.1 保存分摊 ==========
    @Override
    @Transactional
    public List<AllocationVO> saveAllocations(String id, List<AllocationDTO> list) {
        FkReimMain main = requireMainForUpdate(id);
        if (list == null || list.isEmpty()) throw new BusinessException("至少保留一条分摊信息");

        BigDecimal totalRatio = ZERO;
        BigDecimal totalAmount = ZERO;
        for (AllocationDTO dto : list) {
            requireText(dto.getReimCompanyId(), "费用归属公司不能为空");
            requireText(dto.getProjectId(), "项目不能为空");
            totalRatio = totalRatio.add(dto.getAllocationRatio());
            totalAmount = totalAmount.add(dto.getAllocationAmount());
        }
        if (totalRatio.subtract(BigDecimal.ONE).abs().compareTo(new BigDecimal("0.001")) > 0)
            throw new BusinessException("分摊比例合计必须为100%");
        if (totalAmount.subtract(main.getSubsidyTotal()).abs().compareTo(new BigDecimal("0.01")) > 0)
            throw new BusinessException("分摊金额合计必须等于补助总金额");

        allocationMapper.deleteByMainId(id);
        for (AllocationDTO dto : list) {
            allocationMapper.insert(ReimAllocation.builder()
                    .id(Ids.newId()).mainId(id)
                    .reimCompanyId(dto.getReimCompanyId()).reimCompanyNo(dto.getReimCompanyNo()).reimCompanyName(dto.getReimCompanyName())
                    .projectId(dto.getProjectId()).projectNo(dto.getProjectNo()).projectName(dto.getProjectName())
                    .allocationRatio(dto.getAllocationRatio()).allocationAmount(dto.getAllocationAmount()).build());
        }
        return allocationMapper.selectByMainId(id).stream().map(this::toAllocationVO).toList();
    }

    // ========== 4.2 均摊试算 ==========
    @Override
    public List<AllocationVO> calculateEqualAllocations(String id, List<AllocationDTO> list) {
        FkReimMain main = requireMain(id);
        if (list == null || list.isEmpty()) return List.of();

        BigDecimal total = main.getSubsidyTotal() != null ? main.getSubsidyTotal() : ZERO;
        int count = list.size();
        BigDecimal baseAmount = total.divide(new BigDecimal(count), 2, RoundingMode.DOWN);
        BigDecimal remainder = total.subtract(baseAmount.multiply(new BigDecimal(count)));
        BigDecimal baseRatio = BigDecimal.ONE.divide(new BigDecimal(count), 4, RoundingMode.DOWN);

        List<AllocationVO> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            AllocationDTO dto = list.get(i);
            BigDecimal amount = i == 0 ? baseAmount.add(remainder) : baseAmount;
            BigDecimal ratio = i == 0
                    ? BigDecimal.ONE.subtract(baseRatio.multiply(new BigDecimal(count - 1)))
                    : baseRatio;
            result.add(AllocationVO.builder()
                    .reimCompanyId(dto.getReimCompanyId()).reimCompanyNo(dto.getReimCompanyNo()).reimCompanyName(dto.getReimCompanyName())
                    .projectId(dto.getProjectId()).projectNo(dto.getProjectNo()).projectName(dto.getProjectName())
                    .allocationRatio(ratio).allocationAmount(amount).build());
        }
        return result;
    }

    // ==================== private helpers ====================

    private FkReimMain requireMain(String id) {
        FkReimMain main = mainMapper.selectById(id);
        if (main == null) throw new BusinessException("404", "报销单不存在");
        return main;
    }

    private FkReimMain requireMainForUpdate(String id) {
        FkReimMain main = mainMapper.selectByIdForUpdate(id);
        if (main == null) throw new BusinessException("404", "报销单不存在");
        return main;
    }

    private String nextBillNo() {
        mainMapper.getLock("reim_bill_no_lock", 10);
        try {
            int seq = mainMapper.countToday() + 1;
            return "CLBX" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + String.format("%04d", seq);
        } finally {
            mainMapper.releaseLock("reim_bill_no_lock");
        }
    }

    private void validateBase(TravelReimbursementSaveDTO dto) {
        if (dto == null) throw new BusinessException("请求数据不能为空");
        requireText(dto.getReimbursementTitle(), "报销标题不能为空");
        requireText(dto.getBusinessTripReason(), "出差事由不能为空");
        requireText(dto.getReimburserId(), "报销人不能为空");
        requireText(dto.getReimDepartmentId(), "报销部门不能为空");
        requireText(dto.getReimCompanyId(), "费用归属公司不能为空");
        requireText(dto.getBusinessTypeId(), "业务类型不能为空");
        maxLen(dto.getReimbursementTitle(), 500, "报销标题不能超过500字");
        maxLen(dto.getBusinessTripReason(), 500, "出差事由不能超过500字");
        maxLen(dto.getRemarks(), 1000, "备注信息不能超过1000字");
    }

    private void validateTrips(List<TripDTO> trips, boolean required) {
        if (trips == null || trips.isEmpty()) {
            if (required) throw new BusinessException("至少需要一条补录行程");
            return;
        }
        for (TripDTO trip : trips) validateTrip(trip);

        Map<String, List<TripDTO>> byTraveler = trips.stream().collect(Collectors.groupingBy(TripDTO::getTravelerId));
        for (List<TripDTO> tList : byTraveler.values()) {
            tList.sort(Comparator.comparing(TripDTO::getDepartureDate));
            for (int i = 1; i < tList.size(); i++) {
                if (!LocalDate.parse(tList.get(i).getDepartureDate(), DATE_FMT)
                        .isAfter(LocalDate.parse(tList.get(i - 1).getArrivalDate(), DATE_FMT))) {
                    throw new BusinessException("同一出行人的行程日期不能重复或重叠");
                }
            }
        }
    }

    private void validateTrip(TripDTO trip) {
        requireText(trip.getTravelerId(), "出行人不能为空");
        requireText(trip.getDepartureCityNo(), "出发城市不能为空");
        requireText(trip.getArrivalCityNo(), "到达城市不能为空");
        requireText(trip.getTripDescription(), "行程说明不能为空");
        maxLen(trip.getTripDescription(), 500, "行程说明不能超过500字");
        LocalDate start = parseDate(trip.getDepartureDate(), "出发日期格式必须为yyyy-MM-dd");
        LocalDate end = parseDate(trip.getArrivalDate(), "到达日期格式必须为yyyy-MM-dd");
        if (end.isBefore(start)) throw new BusinessException("到达日期不能早于出发日期");
        if (end.isAfter(LocalDate.now())) throw new BusinessException("到达日期不能晚于当前日期");
    }

    private void checkTripOverlap(String mainId, String excludeTripId, TripDTO dto) {
        List<ReimItinerary> existing = itineraryMapper.selectByMainId(mainId);
        LocalDate newStart = LocalDate.parse(dto.getDepartureDate(), DATE_FMT);
        LocalDate newEnd = LocalDate.parse(dto.getArrivalDate(), DATE_FMT);
        for (ReimItinerary it : existing) {
            if (Objects.equals(it.getId(), excludeTripId)) continue;
            if (!dto.getTravelerId().equals(it.getTravelerId())) continue;
            if (!newStart.isAfter(it.getArrivalDate()) && !newEnd.isBefore(it.getDepartureDate())) {
                throw new BusinessException("同一出行人的行程日期不能重复或重叠");
            }
        }
    }

    private FkReimMain buildMain(String id, String billNo, String billStatus, LocalDateTime created, LocalDateTime updated, TravelReimbursementSaveDTO dto) {
        return FkReimMain.builder().id(id).billNo(billNo).billStatus(billStatus)
                .creationTime(created).updateTime(updated)
                .reimbursementTitle(dto.getReimbursementTitle()).businessTripReason(dto.getBusinessTripReason())
                .reimburserId(dto.getReimburserId()).reimburserNo(dto.getReimburserNo()).reimburserName(dto.getReimburserName())
                .reimDepartmentId(dto.getReimDepartmentId()).reimDepartmentNo(dto.getReimDepartmentNo()).reimDepartmentName(dto.getReimDepartmentName())
                .reimCompanyId(dto.getReimCompanyId()).reimCompanyNo(dto.getReimCompanyNo()).reimCompanyName(dto.getReimCompanyName())
                .businessTypeId(dto.getBusinessTypeId()).businessTypeNo(dto.getBusinessTypeNo()).businessTypeName(dto.getBusinessTypeName())
                .remarks(dto.getRemarks()).build();
    }

    private record DetailData(List<ReimItinerary> itineraries, List<ReimSubsidy> subsidies, List<SubsidyCalendar> calendars) {}

    private DetailData buildDetail(String mainId, TravelReimbursementSaveDTO dto) {
        List<ReimItinerary> itineraries = new ArrayList<>();
        List<ReimSubsidy> subsidies = new ArrayList<>();
        List<SubsidyCalendar> calendars = new ArrayList<>();
        if (dto.getTripList() == null) return new DetailData(itineraries, subsidies, calendars);

        for (TripDTO trip : dto.getTripList()) {
            LocalDate start = LocalDate.parse(trip.getDepartureDate(), DATE_FMT);
            LocalDate end = LocalDate.parse(trip.getArrivalDate(), DATE_FMT);
            String tripId = Ids.newId(), subsidyId = Ids.newId();

            List<SubsidyCalendar> cals = buildCalendars(subsidyId, start, end, trip);
            BigDecimal meal = cals.stream().map(SubsidyCalendar::getMealExpensesAmount).reduce(ZERO, BigDecimal::add);
            BigDecimal traffic = cals.stream().map(SubsidyCalendar::getTrafficAmount).reduce(ZERO, BigDecimal::add);
            BigDecimal phone = cals.stream().map(SubsidyCalendar::getCommunicationAmount).reduce(ZERO, BigDecimal::add);
            BigDecimal total = scale(meal.add(traffic).add(phone));

            itineraries.add(buildItinerary(tripId, mainId, subsidyId, trip));
            subsidies.add(ReimSubsidy.builder().id(subsidyId).mainId(mainId)
                    .travelerId(trip.getTravelerId()).travelerNo(trip.getTravelerNo()).travelerName(trip.getTravelerName())
                    .departureDate(start).arrivalDate(end).subsidyDays((int) ChronoUnit.DAYS.between(start, end) + 1)
                    .departureCity(trip.getDepartureCityName()).departureCityNo(trip.getDepartureCityNo())
                    .arrivingCity(trip.getArrivalCityName()).arrivingCityNo(trip.getArrivalCityNo())
                    .applicationAmount(total).subsidyAmount(total)
                    .mealAllowance(scale(meal)).transportationAllowance(scale(traffic)).phoneAllowance(scale(phone))
                    .businessTypeId(dto.getBusinessTypeId()).businessTypeNo(dto.getBusinessTypeNo()).businessTypeName(dto.getBusinessTypeName())
                    .build());
            calendars.addAll(cals);
        }
        return new DetailData(itineraries, subsidies, calendars);
    }

    private List<SubsidyCalendar> buildCalendars(String subsidyId, LocalDate start, LocalDate end, TripDTO trip) {
        String cityType = baseDataMapper.findCityType(trip.getArrivalCityNo());
        if (cityType == null || cityType.isBlank()) throw new BusinessException("到达城市未配置城市类型: " + trip.getArrivalCityName());
        BigDecimal mealStd = mealStandard(cityType);
        List<SubsidyCalendar> list = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            list.add(SubsidyCalendar.builder().id(Ids.newId()).subsidyId(subsidyId)
                    .travelDate(d).travelDateWeek(weekName(d)).subsidizedCities(trip.getArrivalCityName())
                    .subsidizedCityNumber(trip.getArrivalCityNo()).cityType(cityType)
                    .standardMealExpensesAmount(mealStd).standardTrafficAmount(TRANSPORT_STANDARD).standardCommunicationAmount(PHONE_STANDARD)
                    .mealExpensesAmount(mealStd).trafficAmount(TRANSPORT_STANDARD).communicationAmount(PHONE_STANDARD)
                    .mealSelected("1").trafficSelected("1").communicationSelected("1").build());
        }
        return list;
    }

    private ReimItinerary buildItinerary(String tripId, String mainId, String subsidyId, TripDTO trip) {
        return ReimItinerary.builder().id(tripId).mainId(mainId).subsidyId(subsidyId)
                .travelerId(trip.getTravelerId()).travelerNo(trip.getTravelerNo()).travelerName(trip.getTravelerName())
                .departureDate(LocalDate.parse(trip.getDepartureDate(), DATE_FMT))
                .arrivalDate(LocalDate.parse(trip.getArrivalDate(), DATE_FMT))
                .departureCity(trip.getDepartureCityName()).departureCityNo(trip.getDepartureCityNo())
                .arrivingCity(trip.getArrivalCityName()).arrivingCityNo(trip.getArrivalCityNo())
                .itineraryInstructions(trip.getTripDescription()).build();
    }

    private ReimSubsidy buildSubsidy(String subsidyId, String mainId, TripDTO trip) {
        LocalDate start = LocalDate.parse(trip.getDepartureDate(), DATE_FMT);
        LocalDate end = LocalDate.parse(trip.getArrivalDate(), DATE_FMT);
        List<SubsidyCalendar> cals = buildCalendars(subsidyId, start, end, trip);
        BigDecimal meal = cals.stream().map(SubsidyCalendar::getMealExpensesAmount).reduce(ZERO, BigDecimal::add);
        BigDecimal traffic = cals.stream().map(SubsidyCalendar::getTrafficAmount).reduce(ZERO, BigDecimal::add);
        BigDecimal phone = cals.stream().map(SubsidyCalendar::getCommunicationAmount).reduce(ZERO, BigDecimal::add);
        BigDecimal total = scale(meal.add(traffic).add(phone));
        return ReimSubsidy.builder().id(subsidyId).mainId(mainId)
                .travelerId(trip.getTravelerId()).travelerNo(trip.getTravelerNo()).travelerName(trip.getTravelerName())
                .departureDate(start).arrivalDate(end).subsidyDays((int) ChronoUnit.DAYS.between(start, end) + 1)
                .departureCity(trip.getDepartureCityName()).departureCityNo(trip.getDepartureCityNo())
                .arrivingCity(trip.getArrivalCityName()).arrivingCityNo(trip.getArrivalCityNo())
                .applicationAmount(total).subsidyAmount(total)
                .mealAllowance(scale(meal)).transportationAllowance(scale(traffic)).phoneAllowance(scale(phone)).build();
    }

    private void buildAndInsertCalendars(String subsidyId, TripDTO trip) {
        LocalDate start = LocalDate.parse(trip.getDepartureDate(), DATE_FMT);
        LocalDate end = LocalDate.parse(trip.getArrivalDate(), DATE_FMT);
        for (SubsidyCalendar cal : buildCalendars(subsidyId, start, end, trip)) {
            calendarMapper.insert(cal);
        }
    }

    private void applyTotals(FkReimMain main, DetailData detail) {
        BigDecimal meal = detail.subsidies().stream().map(ReimSubsidy::getMealAllowance).reduce(ZERO, BigDecimal::add);
        BigDecimal traffic = detail.subsidies().stream().map(ReimSubsidy::getTransportationAllowance).reduce(ZERO, BigDecimal::add);
        BigDecimal phone = detail.subsidies().stream().map(ReimSubsidy::getPhoneAllowance).reduce(ZERO, BigDecimal::add);
        main.setMealAllowance(scale(meal));
        main.setTransportationAllowance(scale(traffic));
        main.setPhoneAllowance(scale(phone));
        main.setSubsidyTotal(scale(meal.add(traffic).add(phone)));
    }

    private void deleteDetail(String mainId) {
        calendarMapper.deleteByMainId(mainId);
        subsidyMapper.deleteByMainId(mainId);
        itineraryMapper.deleteByMainId(mainId);
        allocationMapper.deleteByMainId(mainId);
    }

    private void insertDetail(String mainId, DetailData detail, TravelReimbursementSaveDTO dto) {
        for (ReimItinerary it : detail.itineraries()) itineraryMapper.insert(it);
        for (ReimSubsidy s : detail.subsidies()) subsidyMapper.insert(s);
        for (SubsidyCalendar c : detail.calendars()) calendarMapper.insert(c);
        if (dto.getAllocationList() != null) {
            for (AllocationDTO a : dto.getAllocationList()) {
                allocationMapper.insert(ReimAllocation.builder()
                        .id(Ids.newId()).mainId(mainId)
                        .reimCompanyId(a.getReimCompanyId()).reimCompanyNo(a.getReimCompanyNo()).reimCompanyName(a.getReimCompanyName())
                        .projectId(a.getProjectId()).projectNo(a.getProjectNo()).projectName(a.getProjectName())
                        .allocationRatio(a.getAllocationRatio()).allocationAmount(a.getAllocationAmount()).build());
            }
        }
    }

    private void recalcMain(String mainId) {
        FkReimMain main = requireMainForUpdate(mainId);
        List<ReimSubsidy> subsidies = subsidyMapper.selectByMainId(mainId);
        BigDecimal meal = subsidies.stream().map(ReimSubsidy::getMealAllowance).reduce(ZERO, BigDecimal::add);
        BigDecimal traffic = subsidies.stream().map(ReimSubsidy::getTransportationAllowance).reduce(ZERO, BigDecimal::add);
        BigDecimal phone = subsidies.stream().map(ReimSubsidy::getPhoneAllowance).reduce(ZERO, BigDecimal::add);
        main.setMealAllowance(scale(meal));
        main.setTransportationAllowance(scale(traffic));
        main.setPhoneAllowance(scale(phone));
        main.setSubsidyTotal(scale(meal.add(traffic).add(phone)));
        main.setUpdateTime(LocalDateTime.now());
        mainMapper.updateDraft(main);
    }

    private void recalcMainFromCalendars(String mainId) {
        FkReimMain main = requireMainForUpdate(mainId);
        List<ReimSubsidy> subsidies = subsidyMapper.selectByMainId(mainId);
        BigDecimal totalMeal = ZERO, totalTraffic = ZERO, totalPhone = ZERO, totalSubsidy = ZERO;
        for (ReimSubsidy sub : subsidies) {
            List<SubsidyCalendar> cals = calendarMapper.selectBySubsidyId(sub.getId());
            BigDecimal meal = cals.stream().filter(c -> "1".equals(c.getMealSelected())).map(SubsidyCalendar::getMealExpensesAmount).reduce(ZERO, BigDecimal::add);
            BigDecimal traffic = cals.stream().filter(c -> "1".equals(c.getTrafficSelected())).map(SubsidyCalendar::getTrafficAmount).reduce(ZERO, BigDecimal::add);
            BigDecimal phone = cals.stream().filter(c -> "1".equals(c.getCommunicationSelected())).map(SubsidyCalendar::getCommunicationAmount).reduce(ZERO, BigDecimal::add);
            totalMeal = totalMeal.add(meal);
            totalTraffic = totalTraffic.add(traffic);
            totalPhone = totalPhone.add(phone);
            totalSubsidy = totalSubsidy.add(meal).add(traffic).add(phone);
            sub.setMealAllowance(scale(meal)); sub.setTransportationAllowance(scale(traffic));
            sub.setPhoneAllowance(scale(phone)); sub.setSubsidyAmount(scale(meal.add(traffic).add(phone)));
            sub.setApplicationAmount(sub.getSubsidyAmount());
            subsidyMapper.update(sub);
        }
        main.setMealAllowance(scale(totalMeal)); main.setTransportationAllowance(scale(totalTraffic));
        main.setPhoneAllowance(scale(totalPhone)); main.setSubsidyTotal(scale(totalSubsidy));
        main.setUpdateTime(LocalDateTime.now());
        mainMapper.updateDraft(main);
    }

    // ==================== VO mapping ====================

    private TravelReimbursementListVO toListVO(FkReimMain m) {
        return TravelReimbursementListVO.builder()
                .id(m.getId()).billNo(m.getBillNo()).billStatus(m.getBillStatus()).billStatusName(BillStatus.nameOf(m.getBillStatus()))
                .reimburserNo(m.getReimburserNo()).reimburserName(m.getReimburserName())
                .reimDepartmentNo(m.getReimDepartmentNo()).reimDepartmentName(m.getReimDepartmentName())
                .reimCompanyName(m.getReimCompanyName()).businessTypeName(m.getBusinessTypeName())
                .reimbursementTitle(m.getReimbursementTitle()).businessTripReason(m.getBusinessTripReason())
                .subsidyTotal(m.getSubsidyTotal()).creationTime(fmt(m.getCreationTime())).build();
    }

    private TripVO toTripVO(ReimItinerary it, String subsidyId) {
        return TripVO.builder().tripId(it.getId()).travelerId(it.getTravelerId()).travelerName(it.getTravelerName())
                .tripDateRange(fmt(it.getDepartureDate()) + "至" + fmt(it.getArrivalDate()))
                .tripRoute(it.getDepartureCity() + "-" + it.getArrivingCity())
                .tripDescription(it.getItineraryInstructions()).subsidyId(subsidyId).build();
    }

    private SubsidyVO toSubsidyVO(ReimSubsidy s) {
        return SubsidyVO.builder().subsidyId(s.getId()).travelerName(s.getTravelerName())
                .travelDateRange(fmt(s.getDepartureDate()) + "至" + fmt(s.getArrivalDate()))
                .subsidyDays(s.getSubsidyDays()).tripRoute(s.getDepartureCity() + "-" + s.getArrivingCity())
                .subsidyCityName(s.getArrivingCity())
                .applicationAmount(scale(s.getApplicationAmount())).subsidyAmount(scale(s.getSubsidyAmount()))
                .mealAllowance(scale(s.getMealAllowance())).transportationAllowance(scale(s.getTransportationAllowance()))
                .phoneAllowance(scale(s.getPhoneAllowance())).build();
    }

    private SubsidyCalendarItemVO toCalendarItemVO(SubsidyCalendar c) {
        return SubsidyCalendarItemVO.builder().calendarId(c.getId())
                .travelDate(fmt(c.getTravelDate())).weekName(c.getTravelDateWeek())
                .subsidyCityName(c.getSubsidizedCities()).cityType(c.getCityType())
                .mealSelected("1".equals(c.getMealSelected())).mealStandard(scale(c.getStandardMealExpensesAmount())).mealAmount(scale(c.getMealExpensesAmount()))
                .transportSelected("1".equals(c.getTrafficSelected())).transportStandard(scale(c.getStandardTrafficAmount())).transportAmount(scale(c.getTrafficAmount()))
                .phoneSelected("1".equals(c.getCommunicationSelected())).phoneStandard(scale(c.getStandardCommunicationAmount())).phoneAmount(scale(c.getCommunicationAmount()))
                .build();
    }

    private AllocationVO toAllocationVO(ReimAllocation a) {
        return AllocationVO.builder().allocationId(a.getId())
                .reimCompanyId(a.getReimCompanyId()).reimCompanyNo(a.getReimCompanyNo()).reimCompanyName(a.getReimCompanyName())
                .projectId(a.getProjectId()).projectNo(a.getProjectNo()).projectName(a.getProjectName())
                .allocationRatio(a.getAllocationRatio()).allocationAmount(a.getAllocationAmount()).build();
    }

    private CostSummaryVO toCostSummary(FkReimMain m) {
        return CostSummaryVO.builder().subsidyTotal(scale(m.getSubsidyTotal()))
                .mealAllowance(scale(m.getMealAllowance())).transportationAllowance(scale(m.getTransportationAllowance()))
                .phoneAllowance(scale(m.getPhoneAllowance())).build();
    }

    // ==================== utilities ====================

    private BigDecimal selectedAmount(SubsidyCalendar c) {
        return ("1".equals(c.getMealSelected()) ? scale(c.getMealExpensesAmount()) : ZERO)
                .add("1".equals(c.getTrafficSelected()) ? scale(c.getTrafficAmount()) : ZERO)
                .add("1".equals(c.getCommunicationSelected()) ? scale(c.getCommunicationAmount()) : ZERO);
    }

    private BigDecimal selectedStandard(SubsidyCalendar c) {
        return ("1".equals(c.getMealSelected()) ? scale(c.getStandardMealExpensesAmount()) : ZERO)
                .add("1".equals(c.getTrafficSelected()) ? scale(c.getStandardTrafficAmount()) : ZERO)
                .add("1".equals(c.getCommunicationSelected()) ? scale(c.getStandardCommunicationAmount()) : ZERO);
    }

    private static BigDecimal mealStandard(String cityType) {
        return switch (cityType) { case "1" -> bd("100"); case "2" -> bd("80"); case "3" -> bd("50"); default -> throw new BusinessException("城市类型不正确: " + cityType); };
    }

    private static BigDecimal scale(BigDecimal v) { return v == null ? ZERO : v.setScale(2, RoundingMode.HALF_UP); }
    private static BigDecimal bd(String s) { return new BigDecimal(s).setScale(2, RoundingMode.HALF_UP); }
    private static String fmt(LocalDate d) { return d == null ? null : d.format(DATE_FMT); }
    private static String fmt(LocalDateTime dt) { return dt == null ? null : dt.format(DATETIME_FMT); }
    private static String weekName(LocalDate d) {
        return switch (d.getDayOfWeek()) { case MONDAY -> "星期一"; case TUESDAY -> "星期二"; case WEDNESDAY -> "星期三"; case THURSDAY -> "星期四"; case FRIDAY -> "星期五"; case SATURDAY -> "星期六"; case SUNDAY -> "星期日"; };
    }
    private static LocalDate parseDate(String v, String msg) { try { return LocalDate.parse(v, DATE_FMT); } catch (Exception e) { throw new BusinessException(msg); } }
    private static void requireText(String v, String msg) { if (v == null || v.isBlank()) throw new BusinessException(msg); }
    private static void maxLen(String v, int max, String msg) { if (v != null && v.length() > max) throw new BusinessException(msg); }
}
