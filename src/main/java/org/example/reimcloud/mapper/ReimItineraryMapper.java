package org.example.reimcloud.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.reimcloud.entity.ReimItinerary;

@Mapper
public interface ReimItineraryMapper {

    int insert(ReimItinerary itinerary);

    List<ReimItinerary> selectByMainId(@Param("mainId") String mainId);

    ReimItinerary selectById(@Param("id") String id);

    int update(ReimItinerary itinerary);

    int deleteById(@Param("id") String id);

    int deleteByMainId(@Param("mainId") String mainId);
}
