package org.example.reimcloud.service;

import java.util.List;

import org.example.reimcloud.dto.AllocationDTO;
import org.example.reimcloud.dto.SubsidyCalendarSaveDTO;
import org.example.reimcloud.dto.TravelReimbursementQueryDTO;
import org.example.reimcloud.dto.TravelReimbursementSaveDTO;
import org.example.reimcloud.dto.TripDTO;
import org.example.reimcloud.vo.AllocationVO;
import org.example.reimcloud.vo.PageVO;
import org.example.reimcloud.vo.SubmitResult;
import org.example.reimcloud.vo.SubsidyCalendarVO;
import org.example.reimcloud.vo.TravelReimbursementCreateResult;
import org.example.reimcloud.vo.TravelReimbursementDetailVO;
import org.example.reimcloud.vo.TravelReimbursementListVO;
import org.example.reimcloud.vo.TravelReimbursementUpdateResult;
import org.example.reimcloud.vo.TripVO;
import org.example.reimcloud.vo.VoidResultVO;

public interface TravelReimbursementService {

    // 1.1 列表
    PageVO<TravelReimbursementListVO> listTravelReimbursements(TravelReimbursementQueryDTO query);

    // 1.2 新增
    TravelReimbursementCreateResult create(TravelReimbursementSaveDTO dto);

    // 1.3 详情
    TravelReimbursementDetailVO getDetail(String id);

    // 1.4 修改
    TravelReimbursementUpdateResult update(String id, TravelReimbursementSaveDTO dto);

    // 1.5 提交
    SubmitResult submit(String id);

    // 1.6 作废
    VoidResultVO voidReimbursement(String id);

    // 2.1-2.3 补录行程
    TripVO createTrip(String id, TripDTO dto);
    TripVO updateTrip(String id, String tripId, TripDTO dto);
    void deleteTrip(String id, String tripId);

    // 3.1-3.2 补助日历
    SubsidyCalendarVO getSubsidyCalendar(String id, String subsidyId);
    void saveSubsidyCalendar(String id, String subsidyId, List<SubsidyCalendarSaveDTO> calendarList);

    // 4.1-4.2 费用分摊
    List<AllocationVO> saveAllocations(String id, List<AllocationDTO> allocationList);
    List<AllocationVO> calculateEqualAllocations(String id, List<AllocationDTO> allocationList);
}
