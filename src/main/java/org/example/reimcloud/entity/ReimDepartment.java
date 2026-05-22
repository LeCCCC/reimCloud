package org.example.reimcloud.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimDepartment {

    private String id;
    private String departmentNo;
    private String departmentName;
}
