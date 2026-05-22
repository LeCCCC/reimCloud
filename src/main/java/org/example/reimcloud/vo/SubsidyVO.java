package org.example.reimcloud.vo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubsidyVO {

    /** 补助信息ID */
    private String subsidyId;

    /** 出行人姓名 */
    private String travelerName;

    /** 出差日期范围 */
    private String travelDateRange;

    /** 补助天数 */
    private Integer subsidyDays;

    /** 行程（出发城市-到达城市） */
    private String tripRoute;

    /** 补助城市 */
    private String subsidyCityName;

    /** 申请金额 */
    private BigDecimal applicationAmount;

    /** 补助金额 */
    private BigDecimal subsidyAmount;

    /** 餐费补助 */
    private BigDecimal mealAllowance;

    /** 交通补助 */
    private BigDecimal transportationAllowance;

    /** 通讯补助 */
    private BigDecimal phoneAllowance;
}
