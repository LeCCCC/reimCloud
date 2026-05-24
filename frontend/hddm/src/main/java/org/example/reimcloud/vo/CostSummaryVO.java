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
public class CostSummaryVO {

    /** 补助总金额 */
    private BigDecimal subsidyTotal;

    /** 餐费补助合计 */
    private BigDecimal mealAllowance;

    /** 交通补助合计 */
    private BigDecimal transportationAllowance;

    /** 通讯补助合计 */
    private BigDecimal phoneAllowance;
}
