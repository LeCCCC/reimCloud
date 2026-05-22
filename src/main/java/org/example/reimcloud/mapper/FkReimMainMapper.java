package org.example.reimcloud.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.reimcloud.dto.TravelReimbursementQueryDTO;
import org.example.reimcloud.entity.FkReimMain;

@Mapper
public interface FkReimMainMapper {

    List<FkReimMain> selectPage(@Param("query") TravelReimbursementQueryDTO query,
                                @Param("offset") int offset);

    long countByQuery(@Param("query") TravelReimbursementQueryDTO query);

    FkReimMain selectById(@Param("id") String id);

    int insert(FkReimMain main);

    int updateDraft(FkReimMain main);

    int updateStatus(FkReimMain main);

    int updateBillStatus(@Param("id") String id, @Param("billStatus") String billStatus);

    int countToday();
}
