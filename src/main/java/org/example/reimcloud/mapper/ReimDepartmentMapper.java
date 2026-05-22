package org.example.reimcloud.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.reimcloud.entity.ReimDepartment;

@Mapper
public interface ReimDepartmentMapper {

    List<ReimDepartment> selectByKeyword(@Param("keyword") String keyword);
}
