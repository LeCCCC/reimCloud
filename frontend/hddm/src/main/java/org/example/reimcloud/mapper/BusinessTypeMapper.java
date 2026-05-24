package org.example.reimcloud.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.example.reimcloud.entity.BusinessType;

@Mapper
public interface BusinessTypeMapper {

    List<BusinessType> selectAll();
}
