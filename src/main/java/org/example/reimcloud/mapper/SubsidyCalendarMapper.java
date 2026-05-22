package org.example.reimcloud.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.reimcloud.entity.SubsidyCalendar;

@Mapper
public interface SubsidyCalendarMapper {

    int insert(SubsidyCalendar calendar);

    int deleteByMainId(@Param("mainId") String mainId);

    List<SubsidyCalendar> selectBySubsidyId(@Param("subsidyId") String subsidyId);
}
