package org.example.reimcloud.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelReimbursementDetailVO {

    /** 报销单主键ID */
    private String id;

    /** 报销单号 */
    private String billNo;

    /** 单据状态：0草稿 1已完成 2已作废 */
    private String billStatus;

    /** 创建时间 */
    private String creationTime;

    /** 报销标题 */
    private String reimbursementTitle;

    /** 出差事由 */
    private String businessTripReason;

    /** 报销人ID */
    private String reimburserId;

    /** 报销人工号 */
    private String reimburserNo;

    /** 报销人姓名 */
    private String reimburserName;

    /** 报销部门ID */
    private String reimDepartmentId;

    /** 报销部门名称 */
    private String reimDepartmentName;

    /** 费用归属公司ID */
    private String reimCompanyId;

    /** 费用归属公司名称 */
    private String reimCompanyName;

    /** 业务类型ID */
    private String businessTypeId;

    /** 业务类型名称 */
    private String businessTypeName;

    /** 补录行程列表 */
    private List<TripVO> tripList;

    /** 补助信息列表 */
    private List<SubsidyVO> subsidyList;

    /** 费用合计信息 */
    private CostSummaryVO costSummary;

    /** 费用归属及分摊列表 */
    private List<AllocationVO> allocationList;

    /** 备注信息 */
    private String remarks;
}
