package org.example.reimcloud.controller;

import org.example.reimcloud.common.ApiResponse;
import org.example.reimcloud.dto.TravelReimbursementQueryDTO;
import org.example.reimcloud.service.TravelReimbursementService;
import org.example.reimcloud.vo.PageVO;
import org.example.reimcloud.vo.TravelReimbursementDetailVO;
import org.example.reimcloud.vo.TravelReimbursementListVO;
import org.example.reimcloud.vo.VoidResultVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    /**
     * 1.3 查询报销单详情
     */
    @GetMapping("/travel-reimbursements/{id}")
    public ApiResponse<TravelReimbursementDetailVO> getDetail(@PathVariable String id) {
        TravelReimbursementDetailVO detail = travelReimbursementService.getDetail(id);
        if (detail == null) {
            return ApiResponse.fail("404", "报销单不存在");
        }
        return ApiResponse.success(detail);
    }

    /**
     * 1.6 作废报销单
     */
    @DeleteMapping("/travel-reimbursements/{id}")
    public ApiResponse<VoidResultVO> voidReimbursement(@PathVariable String id) {
        try {
            VoidResultVO result = travelReimbursementService.voidReimbursement(id);
            if (result == null) {
                return ApiResponse.fail("404", "报销单不存在");
            }
            return ApiResponse.success(result);
        } catch (IllegalStateException e) {
            return ApiResponse.fail("400", e.getMessage());
        }
    }
}
