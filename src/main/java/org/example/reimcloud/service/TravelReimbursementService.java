package org.example.reimcloud.service;

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
import org.example.reimcloud.dto.TravelReimbursementSaveDTO;
import org.example.reimcloud.dto.TripDTO;
import org.example.reimcloud.entity.ReimItinerary;
import org.example.reimcloud.entity.ReimMain;
import org.example.reimcloud.entity.ReimSubsidy;
import org.example.reimcloud.entity.SubsidyCalendar;
import org.example.reimcloud.mapper.BaseDataMapper;
import org.example.reimcloud.mapper.ReimItineraryMapper;
import org.example.reimcloud.mapper.ReimMainMapper;
import org.example.reimcloud.mapper.ReimSubsidyMapper;
import org.example.reimcloud.mapper.SubsidyCalendarMapper;
import org.example.reimcloud.vo.CostSummaryVO;
import org.example.reimcloud.vo.SubmitResult;
import org.example.reimcloud.vo.SubsidyCalendarItemVO;
import org.example.reimcloud.vo.SubsidyCalendarVO;
import org.example.reimcloud.vo.SubsidyVO;
import org.example.reimcloud.vo.TravelReimbursementCreateResult;
import org.example.reimcloud.vo.TravelReimbursementDetailVO;
import org.example.reimcloud.vo.TravelReimbursementUpdateResult;
import org.example.reimcloud.vo.TripVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TravelReimbursementService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal TRANSPORT_STANDARD = money("40");
    private static final BigDecimal PHONE_STANDARD = money("40");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ReimMainMapper reimMainMapper;
    private final ReimItineraryMapper reimItineraryMapper;
    private final ReimSubsidyMapper reimSubsidyMapper;
    private final SubsidyCalendarMapper subsidyCalendarMapper;
    private final BaseDataMapper baseDataMapper;

    public TravelReimbursementService(ReimMainMapper reimMainMapper,
            ReimItineraryMapper reimItineraryMapper,
            ReimSubsidyMapper reimSubsidyMapper,
            SubsidyCalendarMapper subsidyCalendarMapper,
            BaseDataMapper baseDataMapper) {
        this.reimMainMapper = reimMainMapper;
        this.reimItineraryMapper = reimItineraryMapper;
        this.reimSubsidyMapper = reimSubsidyMapper;
        this.subsidyCalendarMapper = subsidyCalendarMapper;
        this.baseDataMapper = baseDataMapper;
    }

    @Transactional
    public TravelReimbursementCreateResult create(TravelReimbursementSaveDTO dto) {
        validateBase(dto);
        validateTrips(dto.getTripList(), false);

        LocalDateTime now = LocalDateTime.now();
        String id = Ids.newId();
        ReimMain main = buildMain(id, nextBillNo(), BillStatus.DRAFT, now, now, dto);
        GeneratedDetail detail = buildGeneratedDetail(id, dto);
        applyTotals(main, detail);

        reimMainMapper.insert(main);
        insertDetail(detail);

        return TravelReimbursementCreateResult.builder()
                .id(id)
                .billNo(main.getBillNo())
                .billStatus(main.getBillStatus())
                .build();
    }

    public TravelReimbursementDetailVO getDetail(String id) {
        ReimMain main = requireMain(id);
        List<ReimItinerary> itineraries = reimItineraryMapper.selectByMainId(id);
        List<ReimSubsidy> subsidies = reimSubsidyMapper.selectByMainId(id);
        Map<String, String> subsidyIdByTripId = itineraries.stream()
                .collect(Collectors.toMap(ReimItinerary::getId, ReimItinerary::getSubsidyId, (a, b) -> a));

        List<TripVO> tripList = itineraries.stream()
                .map(itinerary -> toTripVO(itinerary, subsidyIdByTripId.get(itinerary.getId())))
                .toList();
        List<SubsidyVO> subsidyList = subsidies.stream().map(this::toSubsidyVO).toList();

        return TravelReimbursementDetailVO.builder()
                .id(main.getId())
                .billNo(main.getBillNo())
                .billStatus(main.getBillStatus())
                .creationTime(format(main.getCreationTime()))
                .reimbursementTitle(main.getReimbursementTitle())
                .businessTripReason(main.getBusinessTripReason())
                .reimburserId(main.getReimburserId())
                .reimburserNo(main.getReimburserNo())
                .reimburserName(main.getReimburserName())
                .reimDepartmentId(main.getReimDepartmentId())
                .reimDepartmentName(main.getReimDepartmentName())
                .reimCompanyId(main.getReimCompanyId())
                .reimCompanyName(main.getReimCompanyName())
                .businessTypeId(main.getBusinessTypeId())
                .businessTypeName(main.getBusinessTypeName())
                .tripList(tripList)
                .subsidyList(subsidyList)
                .costSummary(toCostSummary(main))
                .allocationList(List.of())
                .remarks(main.getRemarks())
                .build();
    }

    @Transactional
    public TravelReimbursementUpdateResult update(String id, TravelReimbursementSaveDTO dto) {
        ReimMain old = requireMain(id);
        if (!BillStatus.DRAFT.equals(old.getBillStatus())) {
            throw new BusinessException("只有草稿状态的单据允许修改");
        }
        validateBase(dto);
        validateTrips(dto.getTripList(), false);

        ReimMain main = buildMain(id, old.getBillNo(), old.getBillStatus(), old.getCreationTime(), LocalDateTime.now(), dto);
        GeneratedDetail detail = buildGeneratedDetail(id, dto);
        applyTotals(main, detail);

        deleteDetail(id);
        reimMainMapper.updateDraft(main);
        insertDetail(detail);

        return TravelReimbursementUpdateResult.builder()
                .id(id)
                .billStatus(main.getBillStatus())
                .updateTime(format(main.getUpdateTime()))
                .build();
    }

    @Transactional
    public SubmitResult submit(String id) {
        ReimMain main = requireMain(id);
        if (!BillStatus.DRAFT.equals(main.getBillStatus())) {
            throw new BusinessException("只有草稿状态的单据允许提交");
        }
        List<ReimItinerary> itineraries = reimItineraryMapper.selectByMainId(id);
        if (itineraries.isEmpty()) {
            throw new BusinessException("提交时至少需要一条补录行程");
        }

        main.setBillStatus(BillStatus.COMPLETED);
        main.setUpdateTime(LocalDateTime.now());
        reimMainMapper.updateStatus(main);

        return SubmitResult.builder()
                .id(main.getId())
                .billNo(main.getBillNo())
                .billStatus(main.getBillStatus())
                .submitTime(format(main.getUpdateTime()))
                .build();
    }

    public SubsidyCalendarVO getSubsidyCalendar(String id, String subsidyId) {
        ReimMain main = requireMain(id);
        ReimSubsidy subsidy = reimSubsidyMapper.selectByMainId(id).stream()
                .filter(item -> Objects.equals(item.getId(), subsidyId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("补助信息不存在"));
        List<SubsidyCalendar> calendars = subsidyCalendarMapper.selectBySubsidyId(subsidyId);
        BigDecimal subsidyAmount = calendars.stream()
                .map(this::selectedAmount)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal standardAmount = calendars.stream()
                .map(this::selectedStandard)
                .reduce(ZERO, BigDecimal::add);

        return SubsidyCalendarVO.builder()
                .subsidyId(subsidyId)
                .businessTypeName(main.getBusinessTypeName())
                .startDate(format(subsidy.getDepartureDate()))
                .endDate(format(subsidy.getArrivalDate()))
                .days(subsidy.getSubsidyDays())
                .route(route(subsidy.getDepartureCity(), subsidy.getArrivingCity()))
                .subsidyAmount(scale(subsidyAmount))
                .standardAmount(scale(standardAmount))
                .calendarList(calendars.stream().map(this::toCalendarItemVO).toList())
                .build();
    }

    private ReimMain requireMain(String id) {
        ReimMain main = reimMainMapper.selectById(id);
        if (main == null) {
            throw new BusinessException("报销单不存在");
        }
        return main;
    }

    private void validateBase(TravelReimbursementSaveDTO dto) {
        if (dto == null) {
            throw new BusinessException("请求数据不能为空");
        }
        requireText(dto.getReimbursementTitle(), "报销标题不能为空");
        requireText(dto.getBusinessTripReason(), "出差事由不能为空");
        requireText(dto.getReimburserId(), "报销人不能为空");
        requireText(dto.getReimDepartmentId(), "报销部门不能为空");
        requireText(dto.getReimCompanyId(), "费用归属公司不能为空");
        requireText(dto.getBusinessTypeId(), "业务类型不能为空");
        maxLength(dto.getReimbursementTitle(), 500, "报销标题不能超过500字");
        maxLength(dto.getBusinessTripReason(), 500, "出差事由不能超过500字");
        maxLength(dto.getRemarks(), 1000, "备注信息不能超过1000字");
    }

    private void validateTrips(List<TripDTO> trips, boolean required) {
        if (trips == null || trips.isEmpty()) {
            if (required) {
                throw new BusinessException("至少需要一条补录行程");
            }
            return;
        }
        List<TripRange> ranges = new ArrayList<>();
        for (TripDTO trip : trips) {
            requireText(trip.getTravelerId(), "出行人不能为空");
            requireText(trip.getTravelerNo(), "出行人工号不能为空");
            requireText(trip.getTravelerName(), "出行人姓名不能为空");
            requireText(trip.getDepartureCityNo(), "出发城市不能为空");
            requireText(trip.getDepartureCityName(), "出发城市名称不能为空");
            requireText(trip.getArrivalCityNo(), "到达城市不能为空");
            requireText(trip.getArrivalCityName(), "到达城市名称不能为空");
            requireText(trip.getTripDescription(), "行程说明不能为空");
            maxLength(trip.getTripDescription(), 500, "行程说明不能超过500字");

            LocalDate start = parseDate(trip.getDepartureDate(), "出发日期格式必须为yyyy-MM-dd");
            LocalDate end = parseDate(trip.getArrivalDate(), "到达日期格式必须为yyyy-MM-dd");
            if (end.isBefore(start)) {
                throw new BusinessException("到达日期不能早于出发日期");
            }
            if (end.isAfter(LocalDate.now())) {
                throw new BusinessException("到达日期不能晚于当前日期");
            }
            ranges.add(new TripRange(trip.getTravelerId(), start, end));
        }

        Map<String, List<TripRange>> byTraveler = ranges.stream().collect(Collectors.groupingBy(TripRange::travelerId));
        for (List<TripRange> travelerRanges : byTraveler.values()) {
            travelerRanges.sort(Comparator.comparing(TripRange::start));
            for (int index = 1; index < travelerRanges.size(); index++) {
                TripRange previous = travelerRanges.get(index - 1);
                TripRange current = travelerRanges.get(index);
                if (!current.start().isAfter(previous.end())) {
                    throw new BusinessException("同一出行人的行程日期不能重复或重叠");
                }
            }
        }
    }

    private ReimMain buildMain(String id, String billNo, String billStatus, LocalDateTime creationTime,
            LocalDateTime updateTime, TravelReimbursementSaveDTO dto) {
        return ReimMain.builder()
                .id(id)
                .billNo(billNo)
                .billStatus(billStatus)
                .creationTime(creationTime)
                .updateTime(updateTime)
                .reimbursementTitle(dto.getReimbursementTitle())
                .reimburserId(dto.getReimburserId())
                .reimburserNo(dto.getReimburserNo())
                .reimburserName(dto.getReimburserName())
                .reimDepartmentId(dto.getReimDepartmentId())
                .reimDepartmentNo(dto.getReimDepartmentNo())
                .reimDepartmentName(dto.getReimDepartmentName())
                .reimCompanyId(dto.getReimCompanyId())
                .reimCompanyNo(dto.getReimCompanyNo())
                .reimCompanyName(dto.getReimCompanyName())
                .businessTypeId(dto.getBusinessTypeId())
                .businessTypeNo(dto.getBusinessTypeNo())
                .businessTypeName(dto.getBusinessTypeName())
                .businessTripReason(dto.getBusinessTripReason())
                .remarks(dto.getRemarks())
                .build();
    }

    private GeneratedDetail buildGeneratedDetail(String mainId, TravelReimbursementSaveDTO dto) {
        List<ReimItinerary> itineraries = new ArrayList<>();
        List<ReimSubsidy> subsidies = new ArrayList<>();
        List<SubsidyCalendar> calendars = new ArrayList<>();
        if (dto.getTripList() == null) {
            return new GeneratedDetail(itineraries, subsidies, calendars);
        }

        for (TripDTO trip : dto.getTripList()) {
            LocalDate start = LocalDate.parse(trip.getDepartureDate(), DATE);
            LocalDate end = LocalDate.parse(trip.getArrivalDate(), DATE);
            String tripId = Ids.newId();
            String subsidyId = Ids.newId();
            List<SubsidyCalendar> tripCalendars = buildCalendars(subsidyId, start, end, trip);
            BigDecimal meal = tripCalendars.stream().map(SubsidyCalendar::getMealExpensesAmount).reduce(ZERO, BigDecimal::add);
            BigDecimal traffic = tripCalendars.stream().map(SubsidyCalendar::getTrafficAmount).reduce(ZERO, BigDecimal::add);
            BigDecimal phone = tripCalendars.stream().map(SubsidyCalendar::getCommunicationAmount).reduce(ZERO, BigDecimal::add);
            BigDecimal total = scale(meal.add(traffic).add(phone));

            itineraries.add(ReimItinerary.builder()
                    .id(tripId)
                    .mainId(mainId)
                    .subsidyId(subsidyId)
                    .travelerId(trip.getTravelerId())
                    .travelerNo(trip.getTravelerNo())
                    .travelerName(trip.getTravelerName())
                    .departureDate(start)
                    .arrivalDate(end)
                    .departureCity(trip.getDepartureCityName())
                    .departureCityNo(trip.getDepartureCityNo())
                    .arrivingCity(trip.getArrivalCityName())
                    .arrivingCityNo(trip.getArrivalCityNo())
                    .itineraryInstructions(trip.getTripDescription())
                    .build());
            subsidies.add(ReimSubsidy.builder()
                    .id(subsidyId)
                    .mainId(mainId)
                    .travelerId(trip.getTravelerId())
                    .travelerNo(trip.getTravelerNo())
                    .travelerName(trip.getTravelerName())
                    .departureDate(start)
                    .arrivalDate(end)
                    .subsidyDays((int) ChronoUnit.DAYS.between(start, end) + 1)
                    .departureCity(trip.getDepartureCityName())
                    .departureCityNo(trip.getDepartureCityNo())
                    .arrivingCity(trip.getArrivalCityName())
                    .arrivingCityNo(trip.getArrivalCityNo())
                    .applicationAmount(total)
                    .subsidyAmount(total)
                    .mealAllowance(scale(meal))
                    .transportationAllowance(scale(traffic))
                    .phoneAllowance(scale(phone))
                    .businessTypeId(dto.getBusinessTypeId())
                    .businessTypeNo(dto.getBusinessTypeNo())
                    .businessTypeName(dto.getBusinessTypeName())
                    .build());
            calendars.addAll(tripCalendars);
        }
        return new GeneratedDetail(itineraries, subsidies, calendars);
    }

    private List<SubsidyCalendar> buildCalendars(String subsidyId, LocalDate start, LocalDate end, TripDTO trip) {
        String cityType = baseDataMapper.findCityType(trip.getArrivalCityNo());
        if (cityType == null || cityType.isBlank()) {
            throw new BusinessException("到达城市未配置城市类型：" + trip.getArrivalCityName());
        }
        BigDecimal mealStandard = mealStandard(cityType);
        List<SubsidyCalendar> calendars = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            calendars.add(SubsidyCalendar.builder()
                    .id(Ids.newId())
                    .subsidyId(subsidyId)
                    .travelDate(date)
                    .travelDateWeek(weekName(date))
                    .subsidizedCities(trip.getArrivalCityName())
                    .subsidizedCityNumber(trip.getArrivalCityNo())
                    .cityType(cityType)
                    .standardMealExpensesAmount(mealStandard)
                    .standardTrafficAmount(TRANSPORT_STANDARD)
                    .standardCommunicationAmount(PHONE_STANDARD)
                    .mealExpensesAmount(mealStandard)
                    .trafficAmount(TRANSPORT_STANDARD)
                    .communicationAmount(PHONE_STANDARD)
                    .mealSelected("1")
                    .trafficSelected("1")
                    .communicationSelected("1")
                    .build());
        }
        return calendars;
    }

    private void applyTotals(ReimMain main, GeneratedDetail detail) {
        BigDecimal meal = detail.subsidies().stream().map(ReimSubsidy::getMealAllowance).reduce(ZERO, BigDecimal::add);
        BigDecimal traffic = detail.subsidies().stream().map(ReimSubsidy::getTransportationAllowance).reduce(ZERO, BigDecimal::add);
        BigDecimal phone = detail.subsidies().stream().map(ReimSubsidy::getPhoneAllowance).reduce(ZERO, BigDecimal::add);
        main.setMealAllowance(scale(meal));
        main.setTransportationAllowance(scale(traffic));
        main.setPhoneAllowance(scale(phone));
        main.setSubsidyTotal(scale(meal.add(traffic).add(phone)));
    }

    private void deleteDetail(String mainId) {
        subsidyCalendarMapper.deleteByMainId(mainId);
        reimSubsidyMapper.deleteByMainId(mainId);
        reimItineraryMapper.deleteByMainId(mainId);
    }

    private void insertDetail(GeneratedDetail detail) {
        for (ReimItinerary itinerary : detail.itineraries()) {
            reimItineraryMapper.insert(itinerary);
        }
        for (ReimSubsidy subsidy : detail.subsidies()) {
            reimSubsidyMapper.insert(subsidy);
        }
        for (SubsidyCalendar calendar : detail.calendars()) {
            subsidyCalendarMapper.insert(calendar);
        }
    }

    private TripVO toTripVO(ReimItinerary itinerary, String subsidyId) {
        return TripVO.builder()
                .tripId(itinerary.getId())
                .travelerId(itinerary.getTravelerId())
                .travelerName(itinerary.getTravelerName())
                .tripDateRange(format(itinerary.getDepartureDate()) + "至" + format(itinerary.getArrivalDate()))
                .tripRoute(route(itinerary.getDepartureCity(), itinerary.getArrivingCity()))
                .tripDescription(itinerary.getItineraryInstructions())
                .subsidyId(subsidyId)
                .build();
    }

    private SubsidyVO toSubsidyVO(ReimSubsidy subsidy) {
        return SubsidyVO.builder()
                .subsidyId(subsidy.getId())
                .travelerName(subsidy.getTravelerName())
                .travelDateRange(format(subsidy.getDepartureDate()) + "至" + format(subsidy.getArrivalDate()))
                .subsidyDays(subsidy.getSubsidyDays())
                .tripRoute(route(subsidy.getDepartureCity(), subsidy.getArrivingCity()))
                .subsidyCityName(subsidy.getArrivingCity())
                .applicationAmount(scale(subsidy.getApplicationAmount()))
                .subsidyAmount(scale(subsidy.getSubsidyAmount()))
                .mealAllowance(scale(subsidy.getMealAllowance()))
                .transportationAllowance(scale(subsidy.getTransportationAllowance()))
                .phoneAllowance(scale(subsidy.getPhoneAllowance()))
                .build();
    }

    private SubsidyCalendarItemVO toCalendarItemVO(SubsidyCalendar calendar) {
        return SubsidyCalendarItemVO.builder()
                .calendarId(calendar.getId())
                .travelDate(format(calendar.getTravelDate()))
                .weekName(calendar.getTravelDateWeek())
                .subsidyCityName(calendar.getSubsidizedCities())
                .cityType(calendar.getCityType())
                .mealSelected(selected(calendar.getMealSelected()))
                .mealStandard(scale(calendar.getStandardMealExpensesAmount()))
                .mealAmount(scale(calendar.getMealExpensesAmount()))
                .transportSelected(selected(calendar.getTrafficSelected()))
                .transportStandard(scale(calendar.getStandardTrafficAmount()))
                .transportAmount(scale(calendar.getTrafficAmount()))
                .phoneSelected(selected(calendar.getCommunicationSelected()))
                .phoneStandard(scale(calendar.getStandardCommunicationAmount()))
                .phoneAmount(scale(calendar.getCommunicationAmount()))
                .build();
    }

    private CostSummaryVO toCostSummary(ReimMain main) {
        return CostSummaryVO.builder()
                .subsidyTotal(scale(main.getSubsidyTotal()))
                .mealAllowance(scale(main.getMealAllowance()))
                .transportationAllowance(scale(main.getTransportationAllowance()))
                .phoneAllowance(scale(main.getPhoneAllowance()))
                .build();
    }

    private BigDecimal selectedAmount(SubsidyCalendar calendar) {
        return amountIfSelected(calendar.getMealSelected(), calendar.getMealExpensesAmount())
                .add(amountIfSelected(calendar.getTrafficSelected(), calendar.getTrafficAmount()))
                .add(amountIfSelected(calendar.getCommunicationSelected(), calendar.getCommunicationAmount()));
    }

    private BigDecimal selectedStandard(SubsidyCalendar calendar) {
        return amountIfSelected(calendar.getMealSelected(), calendar.getStandardMealExpensesAmount())
                .add(amountIfSelected(calendar.getTrafficSelected(), calendar.getStandardTrafficAmount()))
                .add(amountIfSelected(calendar.getCommunicationSelected(), calendar.getStandardCommunicationAmount()));
    }

    private BigDecimal amountIfSelected(String selected, BigDecimal amount) {
        return selected(selected) ? scale(amount) : ZERO;
    }

    private String nextBillNo() {
        int sequence = reimMainMapper.countToday() + 1;
        return "CLBX" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + String.format("%04d", sequence);
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
    }

    private static void maxLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new BusinessException(message);
        }
    }

    private static LocalDate parseDate(String value, String message) {
        try {
            return LocalDate.parse(value, DATE);
        } catch (Exception exception) {
            throw new BusinessException(message);
        }
    }

    private static BigDecimal mealStandard(String cityType) {
        return switch (cityType) {
            case "1" -> money("100");
            case "2" -> money("80");
            case "3" -> money("50");
            default -> throw new BusinessException("城市类型不正确：" + cityType);
        };
    }

    private static BigDecimal scale(BigDecimal amount) {
        return amount == null ? ZERO : amount.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static String route(String departureCity, String arrivalCity) {
        return departureCity + "-" + arrivalCity;
    }

    private static boolean selected(String selected) {
        return "1".equals(selected) || "true".equalsIgnoreCase(selected);
    }

    private static String weekName(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "星期一";
            case TUESDAY -> "星期二";
            case WEDNESDAY -> "星期三";
            case THURSDAY -> "星期四";
            case FRIDAY -> "星期五";
            case SATURDAY -> "星期六";
            case SUNDAY -> "星期日";
        };
    }

    private static String format(LocalDate date) {
        return date == null ? null : date.format(DATE);
    }

    private static String format(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATE_TIME);
    }

    private record TripRange(String travelerId, LocalDate start, LocalDate end) {
    }

    private record GeneratedDetail(List<ReimItinerary> itineraries, List<ReimSubsidy> subsidies,
            List<SubsidyCalendar> calendars) {
    }
}
