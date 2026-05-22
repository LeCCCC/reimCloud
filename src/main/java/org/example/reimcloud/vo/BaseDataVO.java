package org.example.reimcloud.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseDataVO {

    /** 主键ID */
    private String id;

    /** 编号 */
    private String no;

    /** 名称 */
    private String name;
}
