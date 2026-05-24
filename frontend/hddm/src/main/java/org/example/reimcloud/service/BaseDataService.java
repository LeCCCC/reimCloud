package org.example.reimcloud.service;

import java.util.List;

import org.example.reimcloud.vo.BaseDataVO;
import org.example.reimcloud.vo.BusinessTypeVO;
import org.example.reimcloud.vo.CityVO;
import org.example.reimcloud.vo.EmployeeVO;
import org.example.reimcloud.vo.ProjectVO;

public interface BaseDataService {

    List<BaseDataVO> listReimCompanies(String keyword);

    List<BaseDataVO> listReimDepartments(String keyword);

    List<EmployeeVO> listEmployees(String keyword);

    List<BusinessTypeVO> listBusinessTypeTree(String superiorId);

    List<CityVO> listCities(String keyword);

    List<ProjectVO> listProjects(String keyword);
}
