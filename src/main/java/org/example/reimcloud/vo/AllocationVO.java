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
public class AllocationVO {

    /** 分摊明细ID */
    private String allocationId;

    /** 费用归属公司ID */
    private String reimCompanyId;

    /** 费用归属公司编号 */
    private String reimCompanyNo;

    /** 费用归属公司名称 */
    private String reimCompanyName;

    /** 项目ID */
    private String projectId;

    /** 项目编号 */
    private String projectNo;

    /** 项目名称 */
    private String projectName;

    /** 分摊比例，区间0-1 */
    private BigDecimal allocationRatio;

    /** 分摊金额 */
    private BigDecimal allocationAmount;
}
