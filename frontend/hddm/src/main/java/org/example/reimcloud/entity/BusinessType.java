package org.example.reimcloud.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessType {

    private String id;
    private String businessTypeNo;
    private String businessTypeName;
    private String thereSubordinateNode;
    private String superiorId;
}
