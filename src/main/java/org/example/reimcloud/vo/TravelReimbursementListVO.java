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
public class TravelReimbursementListVO {

    /** 主键ID */
    private String id;

    /** 报销单号 */
    private String billNo;

    /** 单据状态编码：0草稿 1已完成 2已作废 */
    private String billStatus;

    /** 单据状态名称 */
    private String billStatusName;

    /** 报销人工号 */
    private String reimburserNo;

    /** 报销人姓名 */
    private String reimburserName;

    /** 报销部门编号 */
    private String reimDepartmentNo;

    /** 报销部门名称 */
    private String reimDepartmentName;

    /** 费用归属公司名称 */
    private String reimCompanyName;

    /** 业务类型名称 */
    private String businessTypeName;

    /** 报销标题 */
    private String reimbursementTitle;

    /** 报销事由 */
    private String businessTripReason;

    /** 补助金额 */
    private BigDecimal subsidyTotal;

    /** 创建时间 */
    private String creationTime;
}
