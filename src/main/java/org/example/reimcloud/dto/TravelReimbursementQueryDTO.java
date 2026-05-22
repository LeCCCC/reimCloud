package org.example.reimcloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelReimbursementQueryDTO {

    /** 当前页，默认1 */
    private Integer current;

    /** 每页大小，默认10 */
    private Integer size;

    /** 报销单号，模糊查询 */
    private String billNo;

    /** 报销标题，模糊查询 */
    private String reimbursementTitle;

    /** 出差事由，模糊查询 */
    private String businessTripReason;

    /** 费用归属公司ID */
    private String reimCompanyId;

    /** 报销部门ID */
    private String reimDepartmentId;

    /** 报销人ID */
    private String reimburserId;

    /** 业务类型ID */
    private String businessTypeId;

    /** 单据状态：0草稿 1已完成 2已作废 */
    private String billStatus;
}
