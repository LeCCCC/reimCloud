package org.example.reimcloud.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.reimcloud.entity.ReimSubsidy;

@Mapper
public interface ReimSubsidyMapper {

    int insert(ReimSubsidy subsidy);

    List<ReimSubsidy> selectByMainId(@Param("mainId") String mainId);

    ReimSubsidy selectById(@Param("id") String id);

    int update(ReimSubsidy subsidy);

    int deleteByMainId(@Param("mainId") String mainId);
}
