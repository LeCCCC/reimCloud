package org.example.reimcloud.controller;

import java.util.List;

import org.example.reimcloud.common.ApiResponse;
import org.example.reimcloud.service.BaseDataService;
import org.example.reimcloud.vo.BaseDataVO;
import org.example.reimcloud.vo.BusinessTypeVO;
import org.example.reimcloud.vo.CityVO;
import org.example.reimcloud.vo.EmployeeVO;
import org.example.reimcloud.vo.ProjectVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BaseDataController {

    private final BaseDataService baseDataService;

    public BaseDataController(BaseDataService baseDataService) {
        this.baseDataService = baseDataService;
    }

    @GetMapping("/reim-companies")
    public ApiResponse<List<BaseDataVO>> listReimCompanies(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(baseDataService.listReimCompanies(keyword));
    }

    @GetMapping("/reim-departments")
    public ApiResponse<List<BaseDataVO>> listReimDepartments(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(baseDataService.listReimDepartments(keyword));
    }

    @GetMapping("/employees")
    public ApiResponse<List<EmployeeVO>> listEmployees(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(baseDataService.listEmployees(keyword));
    }

    @GetMapping("/business-types/tree")
    public ApiResponse<List<BusinessTypeVO>> listBusinessTypeTree(@RequestParam(required = false) String superiorId) {
        return ApiResponse.success(baseDataService.listBusinessTypeTree(superiorId));
    }

    @GetMapping("/cities")
    public ApiResponse<List<CityVO>> listCities(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(baseDataService.listCities(keyword));
    }

    @GetMapping("/projects")
    public ApiResponse<List<ProjectVO>> listProjects(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(baseDataService.listProjects(keyword));
    }
}
