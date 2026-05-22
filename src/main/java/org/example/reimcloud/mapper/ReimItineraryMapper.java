package org.example.reimcloud.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.reimcloud.entity.ReimItinerary;

@Mapper
public interface ReimItineraryMapper {

    int insert(ReimItinerary itinerary);

    int deleteByMainId(@Param("mainId") String mainId);

    List<ReimItinerary> selectByMainId(@Param("mainId") String mainId);
}
