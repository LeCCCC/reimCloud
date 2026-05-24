package org.example.reimcloud.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.reimcloud.entity.ReimCompany;

@Mapper
public interface ReimCompanyMapper {

    List<ReimCompany> selectByKeyword(@Param("keyword") String keyword);
}
