package org.example.reimcloud.service;

import java.util.List;

import org.example.reimcloud.dto.TravelReimbursementQueryDTO;
import org.example.reimcloud.entity.BusinessType;
import org.example.reimcloud.entity.Employee;
import org.example.reimcloud.entity.ReimCompany;
import org.example.reimcloud.entity.ReimDepartment;
import org.example.reimcloud.vo.BusinessTypeVO;
import org.example.reimcloud.vo.PageVO;
import org.example.reimcloud.vo.TravelReimbursementDetailVO;
import org.example.reimcloud.vo.TravelReimbursementListVO;
import org.example.reimcloud.vo.VoidResultVO;

public interface TravelReimbursementService {

    /**
     * 分页查询报销单列表
     */
    PageVO<TravelReimbursementListVO> listTravelReimbursements(TravelReimbursementQueryDTO query);

    /**
     * 查询报销单详情
     */
    TravelReimbursementDetailVO getDetail(String id);

    /**
     * 作废报销单
     */
    VoidResultVO voidReimbursement(String id);

    /**
     * 查询费用归属公司列表
     */
    List<ReimCompany> listCompanies(String keyword);

    /**
     * 查询报销部门列表
     */
    List<ReimDepartment> listDepartments(String keyword);

    /**
     * 查询员工列表
     */
    List<Employee> listEmployees(String keyword);

    /**
     * 查询业务类型树
     */
    List<BusinessTypeVO> listBusinessTypeTree();
}
