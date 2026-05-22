package org.example.reimcloud.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.reimcloud.entity.ReimMain;

@Mapper
public interface ReimMainMapper {

    int insert(ReimMain main);

    int updateDraft(ReimMain main);

    int updateStatus(ReimMain main);

    ReimMain selectById(@Param("id") String id);

    int countToday();
}
