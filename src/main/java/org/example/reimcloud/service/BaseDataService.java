package org.example.reimcloud.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.example.reimcloud.mapper.BaseDataMapper;
import org.example.reimcloud.vo.BaseDataVO;
import org.example.reimcloud.vo.BusinessTypeVO;
import org.example.reimcloud.vo.CityVO;
import org.example.reimcloud.vo.EmployeeVO;
import org.example.reimcloud.vo.ProjectVO;
import org.springframework.stereotype.Service;

@Service
public class BaseDataService {

    private final BaseDataMapper baseDataMapper;

    public BaseDataService(BaseDataMapper baseDataMapper) {
        this.baseDataMapper = baseDataMapper;
    }

    public List<BaseDataVO> listReimCompanies(String keyword) {
        return baseDataMapper.listReimCompanies(keyword);
    }

    public List<BaseDataVO> listReimDepartments(String keyword) {
        return baseDataMapper.listReimDepartments(keyword);
    }

    public List<EmployeeVO> listEmployees(String keyword) {
        return baseDataMapper.listEmployees(keyword);
    }

    public List<BusinessTypeVO> listBusinessTypeTree(String superiorId) {
        List<BusinessTypeVO> rows = baseDataMapper.listBusinessTypes(null);
        Map<String, BusinessTypeVO> byId = new LinkedHashMap<>();
        for (BusinessTypeVO row : rows) {
            row.setChildren(new ArrayList<>());
            byId.put(row.getBusinessTypeId(), row);
        }

        List<BusinessTypeVO> roots = new ArrayList<>();
        for (BusinessTypeVO row : rows) {
            BusinessTypeVO parent = byId.get(row.getSuperiorId());
            if (parent == null || "none".equals(row.getSuperiorId())) {
                roots.add(row);
            } else {
                parent.getChildren().add(row);
            }
        }

        if (superiorId == null || superiorId.isBlank()) {
            return roots;
        }
        if ("none".equals(superiorId)) {
            return roots;
        }
        BusinessTypeVO node = byId.get(superiorId);
        return node == null ? List.of() : node.getChildren();
    }

    public List<CityVO> listCities(String keyword) {
        return baseDataMapper.listCities(keyword);
    }

    public List<ProjectVO> listProjects(String keyword) {
        return baseDataMapper.listProjects(keyword);
    }
}
