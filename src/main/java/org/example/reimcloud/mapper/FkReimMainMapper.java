package org.example.reimcloud.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.reimcloud.dto.TravelReimbursementQueryDTO;
import org.example.reimcloud.entity.FkReimMain;

@Mapper
public interface FkReimMainMapper {

    /**
     * 分页查询报销单列表（含模糊筛选）
     */
    List<FkReimMain> selectPage(@Param("query") TravelReimbursementQueryDTO query,
                                @Param("offset") int offset);

    /**
     * 统计符合条件的总条数
     */
    long countByQuery(@Param("query") TravelReimbursementQueryDTO query);

    /**
     * 根据主键ID查询报销单
     */
    FkReimMain selectById(@Param("id") String id);

    /**
     * 更新单据状态
     */
    int updateBillStatus(@Param("id") String id, @Param("billStatus") String billStatus);
}
