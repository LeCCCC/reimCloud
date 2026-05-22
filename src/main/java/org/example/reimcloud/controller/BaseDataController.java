package org.example.reimcloud.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.example.reimcloud.common.ApiResponse;
import org.example.reimcloud.service.TravelReimbursementService;
import org.example.reimcloud.vo.BaseDataVO;
import org.example.reimcloud.vo.BusinessTypeVO;
import org.example.reimcloud.vo.EmployeeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BaseDataController {

    private final TravelReimbursementService travelReimbursementService;

    /**
     * 5.1 查询费用归属公司列表
     */
    @GetMapping("/reim-companies")
    public ApiResponse<List<BaseDataVO>> listCompanies(
            @RequestParam(required = false) String keyword) {
        List<BaseDataVO> list = travelReimbursementService.listCompanies(keyword).stream()
                .map(e -> BaseDataVO.builder()
                        .id(e.getId())
                        .no(e.getCompanyNo())
                        .name(e.getCompanyName())
                        .build())
                .collect(Collectors.toList());
        return ApiResponse.success(list);
    }

    /**
     * 5.2 查询报销部门列表
     */
    @GetMapping("/reim-departments")
    public ApiResponse<List<BaseDataVO>> listDepartments(
            @RequestParam(required = false) String keyword) {
        List<BaseDataVO> list = travelReimbursementService.listDepartments(keyword).stream()
                .map(e -> BaseDataVO.builder()
                        .id(e.getId())
                        .no(e.getDepartmentNo())
                        .name(e.getDepartmentName())
                        .build())
                .collect(Collectors.toList());
        return ApiResponse.success(list);
    }

    /**
     * 5.3 查询员工列表
     */
    @GetMapping("/employees")
    public ApiResponse<List<EmployeeVO>> listEmployees(
            @RequestParam(required = false) String keyword) {
        List<EmployeeVO> list = travelReimbursementService.listEmployees(keyword).stream()
                .map(e -> EmployeeVO.builder()
                        .reimburserId(e.getId())
                        .reimburserNo(e.getEmployeeNo())
                        .reimburserName(e.getEmployeeName())
                        .build())
                .collect(Collectors.toList());
        return ApiResponse.success(list);
    }

    /**
     * 5.4 查询业务类型树
     */
    @GetMapping("/business-types/tree")
    public ApiResponse<List<BusinessTypeVO>> listBusinessTypeTree() {
        List<BusinessTypeVO> tree = travelReimbursementService.listBusinessTypeTree();
        return ApiResponse.success(tree);
    }
}
