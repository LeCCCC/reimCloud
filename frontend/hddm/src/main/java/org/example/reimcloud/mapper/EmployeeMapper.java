package org.example.reimcloud.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.reimcloud.entity.Employee;

@Mapper
public interface EmployeeMapper {

    List<Employee> selectByKeyword(@Param("keyword") String keyword);
}
