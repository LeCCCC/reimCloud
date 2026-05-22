package org.example.reimcloud.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoidResultVO {

    /** 报销单主键ID */
    private String id;

    /** 作废后单据状态 */
    private String billStatus;
}
