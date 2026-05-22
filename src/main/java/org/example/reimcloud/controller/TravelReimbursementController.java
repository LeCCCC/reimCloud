package org.example.reimcloud.controller;

import org.example.reimcloud.common.ApiResponse;
import org.example.reimcloud.dto.TravelReimbursementSaveDTO;
import org.example.reimcloud.service.TravelReimbursementService;
import org.example.reimcloud.vo.SubmitResult;
import org.example.reimcloud.vo.SubsidyCalendarVO;
import org.example.reimcloud.vo.TravelReimbursementCreateResult;
import org.example.reimcloud.vo.TravelReimbursementDetailVO;
import org.example.reimcloud.vo.TravelReimbursementUpdateResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/travel-reimbursements")
public class TravelReimbursementController {

    private final TravelReimbursementService travelReimbursementService;

    public TravelReimbursementController(TravelReimbursementService travelReimbursementService) {
        this.travelReimbursementService = travelReimbursementService;
    }

    @PostMapping
    public ApiResponse<TravelReimbursementCreateResult> createTravelReimbursement(
            @RequestBody TravelReimbursementSaveDTO dto) {
        return ApiResponse.success(travelReimbursementService.create(dto));
    }

    @GetMapping("/{id}")
    public ApiResponse<TravelReimbursementDetailVO> getTravelReimbursementDetail(@PathVariable String id) {
        return ApiResponse.success(travelReimbursementService.getDetail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<TravelReimbursementUpdateResult> updateTravelReimbursement(@PathVariable String id,
            @RequestBody TravelReimbursementSaveDTO dto) {
        return ApiResponse.success(travelReimbursementService.update(id, dto));
    }

    @PostMapping("/{id}/submission")
    public ApiResponse<SubmitResult> submitTravelReimbursement(@PathVariable String id) {
        return ApiResponse.success(travelReimbursementService.submit(id));
    }

    @GetMapping("/{id}/subsidies/{subsidyId}/calendar")
    public ApiResponse<SubsidyCalendarVO> getSubsidyCalendar(@PathVariable String id, @PathVariable String subsidyId) {
        return ApiResponse.success(travelReimbursementService.getSubsidyCalendar(id, subsidyId));
    }
}
