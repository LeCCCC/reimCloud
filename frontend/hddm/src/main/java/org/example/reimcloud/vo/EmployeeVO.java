package org.example.reimcloud.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeVO {

    /** 员工ID */
    private String reimburserId;

    /** 员工工号 */
    private String reimburserNo;

    /** 员工姓名 */
    private String reimburserName;
}
