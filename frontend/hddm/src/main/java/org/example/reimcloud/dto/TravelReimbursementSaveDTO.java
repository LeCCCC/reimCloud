package org.example.reimcloud.dto;

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
public class TravelReimbursementSaveDTO {

    /** 报销标题，最大500字 */
    private String reimbursementTitle;

    /** 出差事由，最大500字 */
    private String businessTripReason;

    /** 报销人ID */
    private String reimburserId;

    /** 报销人工号 */
    private String reimburserNo;

    /** 报销人姓名 */
    private String reimburserName;

    /** 报销部门ID */
    private String reimDepartmentId;

    /** 报销部门编号 */
    private String reimDepartmentNo;

    /** 报销部门名称 */
    private String reimDepartmentName;

    /** 费用归属公司ID */
    private String reimCompanyId;

    /** 费用归属公司编号 */
    private String reimCompanyNo;

    /** 费用归属公司名称 */
    private String reimCompanyName;

    /** 业务类型ID */
    private String businessTypeId;

    /** 业务类型编号 */
    private String businessTypeNo;

    /** 业务类型名称 */
    private String businessTypeName;

    /** 补录行程集合，草稿可为空，提交必须至少一条 */
    private List<TripDTO> tripList;

    /** 补助信息集合，通常由行程自动生成后保存 */
    private List<SubsidyDTO> subsidyList;

    /** 费用归属及分摊集合 */
    private List<AllocationDTO> allocationList;

    /** 备注信息，最大1000字 */
    private String remarks;
}
