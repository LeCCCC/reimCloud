package org.example.reimcloud.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.reimcloud.entity.ReimAllocation;

@Mapper
public interface ReimAllocationMapper {

    int insert(ReimAllocation allocation);

    List<ReimAllocation> selectByMainId(@Param("mainId") String mainId);

    int deleteByMainId(@Param("mainId") String mainId);
}
