package org.example.reimcloud.controller;

import java.util.List;

import org.example.reimcloud.common.ApiResponse;
import org.example.reimcloud.dto.AllocationDTO;
import org.example.reimcloud.dto.SubsidyCalendarSaveDTO;
import org.example.reimcloud.dto.TravelReimbursementQueryDTO;
import org.example.reimcloud.dto.TravelReimbursementSaveDTO;
import org.example.reimcloud.dto.TripDTO;
import org.example.reimcloud.service.TravelReimbursementService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TravelReimbursementController {

    private final TravelReimbursementService service;

    /** 1.1 列表 */
    @GetMapping("/travel-reimbursements")
    public ApiResponse<PageVO<TravelReimbursementListVO>> list(TravelReimbursementQueryDTO query) {
        return ApiResponse.success(service.listTravelReimbursements(query));
    }

    /** 1.2 新增 */
    @PostMapping("/travel-reimbursements")
    public ApiResponse<TravelReimbursementCreateResult> create(@RequestBody TravelReimbursementSaveDTO dto) {
        return ApiResponse.success(service.create(dto));
    }

    /** 1.3 详情 */
    @GetMapping("/travel-reimbursements/{id}")
    public ApiResponse<TravelReimbursementDetailVO> detail(@PathVariable String id) {
        return ApiResponse.success(service.getDetail(id));
    }

    /** 1.4 修改 */
    @PutMapping("/travel-reimbursements/{id}")
    public ApiResponse<TravelReimbursementUpdateResult> update(@PathVariable String id,
            @RequestBody TravelReimbursementSaveDTO dto) {
        return ApiResponse.success(service.update(id, dto));
    }

    /** 1.5 提交 */
    @PostMapping("/travel-reimbursements/{id}/submission")
    public ApiResponse<SubmitResult> submit(@PathVariable String id) {
        return ApiResponse.success(service.submit(id));
    }

    /** 1.6 作废 */
    @DeleteMapping("/travel-reimbursements/{id}")
    public ApiResponse<VoidResultVO> void_(@PathVariable String id) {
        return ApiResponse.success(service.voidReimbursement(id));
    }

    /** 2.1 新增行程 */
    @PostMapping("/travel-reimbursements/{id}/trips")
    public ApiResponse<TripVO> createTrip(@PathVariable String id, @RequestBody TripDTO dto) {
        return ApiResponse.success(service.createTrip(id, dto));
    }

    /** 2.2 修改行程 */
    @PutMapping("/travel-reimbursements/{id}/trips/{tripId}")
    public ApiResponse<TripVO> updateTrip(@PathVariable String id, @PathVariable String tripId,
            @RequestBody TripDTO dto) {
        return ApiResponse.success(service.updateTrip(id, tripId, dto));
    }

    /** 2.3 删除行程 */
    @DeleteMapping("/travel-reimbursements/{id}/trips/{tripId}")
    public ApiResponse<Void> deleteTrip(@PathVariable String id, @PathVariable String tripId) {
        service.deleteTrip(id, tripId);
        return ApiResponse.success();
    }

    /** 3.1 查询补助日历 */
    @GetMapping("/travel-reimbursements/{id}/subsidies/{subsidyId}/calendar")
    public ApiResponse<SubsidyCalendarVO> getCalendar(@PathVariable String id, @PathVariable String subsidyId) {
        return ApiResponse.success(service.getSubsidyCalendar(id, subsidyId));
    }

    /** 3.2 保存补助日历 */
    @PutMapping("/travel-reimbursements/{id}/subsidies/{subsidyId}/calendar")
    public ApiResponse<Void> saveCalendar(@PathVariable String id, @PathVariable String subsidyId,
            @RequestBody List<SubsidyCalendarSaveDTO> calendarList) {
        service.saveSubsidyCalendar(id, subsidyId, calendarList);
        return ApiResponse.success();
    }

    /** 4.1 保存分摊 */
    @PutMapping("/travel-reimbursements/{id}/allocations")
    public ApiResponse<List<AllocationVO>> saveAllocations(@PathVariable String id,
            @RequestBody List<AllocationDTO> list) {
        return ApiResponse.success(service.saveAllocations(id, list));
    }

    /** 4.2 均摊试算 */
    @PostMapping("/travel-reimbursements/{id}/allocation-equalizations")
    public ApiResponse<List<AllocationVO>> equalAllocations(@PathVariable String id,
            @RequestBody List<AllocationDTO> list) {
        return ApiResponse.success(service.calculateEqualAllocations(id, list));
    }
}
