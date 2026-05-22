package org.example.reimcloud.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.reimcloud.dto.TravelReimbursementQueryDTO;
import org.example.reimcloud.entity.BusinessType;
import org.example.reimcloud.entity.Employee;
import org.example.reimcloud.entity.FkReimMain;
import org.example.reimcloud.entity.ReimCompany;
import org.example.reimcloud.entity.ReimDepartment;
import org.example.reimcloud.mapper.BusinessTypeMapper;
import org.example.reimcloud.mapper.EmployeeMapper;
import org.example.reimcloud.mapper.FkReimMainMapper;
import org.example.reimcloud.mapper.ReimCompanyMapper;
import org.example.reimcloud.mapper.ReimDepartmentMapper;
import org.example.reimcloud.service.TravelReimbursementService;
import org.example.reimcloud.vo.BusinessTypeVO;
import org.example.reimcloud.vo.PageVO;
import org.example.reimcloud.vo.TravelReimbursementListVO;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelReimbursementServiceImpl implements TravelReimbursementService {

    private final FkReimMainMapper fkReimMainMapper;
    private final ReimCompanyMapper reimCompanyMapper;
    private final ReimDepartmentMapper reimDepartmentMapper;
    private final EmployeeMapper employeeMapper;
    private final BusinessTypeMapper businessTypeMapper;

    @Override
    public PageVO<TravelReimbursementListVO> listTravelReimbursements(TravelReimbursementQueryDTO query) {
        int current = query.getCurrent() != null ? query.getCurrent() : 1;
        int size = query.getSize() != null ? query.getSize() : 10;
        int offset = (current - 1) * size;

        long total = fkReimMainMapper.countByQuery(query);
        List<FkReimMain> list = fkReimMainMapper.selectPage(query, offset);

        List<TravelReimbursementListVO> records = list.stream()
                .map(this::toListVO)
                .collect(Collectors.toList());

        long pages = (total + size - 1) / size;
        return PageVO.<TravelReimbursementListVO>builder()
                .total(total)
                .pages(pages)
                .current((long) current)
                .size((long) size)
                .records(records)
                .build();
    }

    @Override
    public List<ReimCompany> listCompanies(String keyword) {
        return reimCompanyMapper.selectByKeyword(keyword);
    }

    @Override
    public List<ReimDepartment> listDepartments(String keyword) {
        return reimDepartmentMapper.selectByKeyword(keyword);
    }

    @Override
    public List<Employee> listEmployees(String keyword) {
        return employeeMapper.selectByKeyword(keyword);
    }

    @Override
    public List<BusinessTypeVO> listBusinessTypeTree() {
        List<BusinessType> all = businessTypeMapper.selectAll();
        Map<String, List<BusinessType>> parentMap = all.stream()
                .collect(Collectors.groupingBy(BusinessType::getSuperiorId));

        return buildTree("none", parentMap);
    }

    private List<BusinessTypeVO> buildTree(String parentId, Map<String, List<BusinessType>> parentMap) {
        List<BusinessType> children = parentMap.get(parentId);
        if (children == null) {
            return new ArrayList<>();
        }
        return children.stream()
                .map(bt -> {
                    BusinessTypeVO vo = BusinessTypeVO.builder()
                            .businessTypeId(bt.getId())
                            .businessTypeNo(bt.getBusinessTypeNo())
                            .businessTypeName(bt.getBusinessTypeName())
                            .thereSubordinateNode(bt.getThereSubordinateNode())
                            .superiorId(bt.getSuperiorId())
                            .children(buildTree(bt.getId(), parentMap))
                            .build();
                    if (vo.getChildren().isEmpty()) {
                        vo.setChildren(null);
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private TravelReimbursementListVO toListVO(FkReimMain entity) {
        return TravelReimbursementListVO.builder()
                .id(entity.getId())
                .billNo(entity.getBillNo())
                .billStatus(entity.getBillStatus())
                .billStatusName(billStatusName(entity.getBillStatus()))
                .reimburserNo(entity.getReimburserNo())
                .reimburserName(entity.getReimburserName())
                .reimDepartmentNo(entity.getReimDepartmentNo())
                .reimDepartmentName(entity.getReimDepartmentName())
                .reimCompanyName(entity.getReimCompanyName())
                .businessTypeName(entity.getBusinessTypeName())
                .reimbursementTitle(entity.getReimbursementTitle())
                .businessTripReason(entity.getBusinessTripReason())
                .subsidyTotal(entity.getSubsidyTotal())
                .creationTime(entity.getCreationTime() != null ? entity.getCreationTime().toString() : null)
                .build();
    }

    private String billStatusName(String status) {
        if (status == null) return "";
        return switch (status) {
            case "0" -> "草稿";
            case "1" -> "已完成";
            case "2" -> "已作废";
            default -> "";
        };
    }
}
