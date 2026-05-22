package org.example.reimcloud.controller;

import org.example.reimcloud.common.ApiResponse;
import org.example.reimcloud.dto.TravelReimbursementQueryDTO;
import org.example.reimcloud.service.TravelReimbursementService;
import org.example.reimcloud.vo.PageVO;
import org.example.reimcloud.vo.TravelReimbursementListVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TravelReimbursementController {

    private final TravelReimbursementService travelReimbursementService;

    /**
     * 1.1 查询报销单分页列表
     */
    @GetMapping("/travel-reimbursements")
    public ApiResponse<PageVO<TravelReimbursementListVO>> listTravelReimbursements(
            TravelReimbursementQueryDTO query) {
        PageVO<TravelReimbursementListVO> page = travelReimbursementService.listTravelReimbursements(query);
        return ApiResponse.success(page);
    }
}
