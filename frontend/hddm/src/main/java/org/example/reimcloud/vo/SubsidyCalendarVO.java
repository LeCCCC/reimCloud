package org.example.reimcloud.vo;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubsidyCalendarVO {

    /** 补助信息ID */
    private String subsidyId;

    /** 出差类型，与主单业务类型一致 */
    private String businessTypeName;

    /** 开始日期 yyyy-MM-dd */
    private String startDate;

    /** 结束日期 yyyy-MM-dd */
    private String endDate;

    /** 天数 */
    private Integer days;

    /** 行程地点（出发城市-到达城市） */
    private String route;

    /** 已选补助金额合计 */
    private BigDecimal subsidyAmount;

    /** 已选标准金额合计 */
    private BigDecimal standardAmount;

    /** 补助日历明细列表 */
    private List<SubsidyCalendarItemVO> calendarList;
}
