package org.example.reimcloud.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.reimcloud.vo.BaseDataVO;
import org.example.reimcloud.vo.BusinessTypeVO;
import org.example.reimcloud.vo.CityVO;
import org.example.reimcloud.vo.EmployeeVO;
import org.example.reimcloud.vo.ProjectVO;

@Mapper
public interface BaseDataMapper {

    List<BaseDataVO> listReimCompanies(@Param("keyword") String keyword);

    List<BaseDataVO> listReimDepartments(@Param("keyword") String keyword);

    List<EmployeeVO> listEmployees(@Param("keyword") String keyword);

    List<BusinessTypeVO> listBusinessTypes(@Param("superiorId") String superiorId);

    List<CityVO> listCities(@Param("keyword") String keyword);

    List<ProjectVO> listProjects(@Param("keyword") String keyword);

    String findCityType(@Param("cityNo") String cityNo);
}
