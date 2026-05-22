package org.example.reimcloud.entity;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimAllocation {

    private String id;
    private String mainId;
    private String reimCompanyId;
    private String reimCompanyNo;
    private String reimCompanyName;
    private String projectId;
    private String projectNo;
    private String projectName;
    private BigDecimal allocationRatio;
    private BigDecimal allocationAmount;
}
