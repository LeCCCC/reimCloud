package org.example.reimcloud.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FkReimMain {

    private String id;
    private String billNo;
    private String billStatus;
    private LocalDateTime creationTime;
    private String reimbursementTitle;
    private String reimburserId;
    private String reimburserNo;
    private String reimburserName;
    private String reimDepartmentId;
    private String reimDepartmentNo;
    private String reimDepartmentName;
    private String reimCompanyId;
    private String reimCompanyNo;
    private String reimCompanyName;
    private String businessTypeId;
    private String businessTypeNo;
    private String businessTypeName;
    private String businessTripReason;
    private BigDecimal subsidyTotal;
    private BigDecimal mealAllowance;
    private BigDecimal transportationAllowance;
    private BigDecimal phoneAllowance;
    private String remarks;
}
