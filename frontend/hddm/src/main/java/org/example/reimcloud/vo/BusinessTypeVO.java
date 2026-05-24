package org.example.reimcloud.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BusinessTypeVO {

    /** 业务类型ID */
    private String businessTypeId;

    /** 业务类型编号 */
    private String businessTypeNo;

    /** 业务类型名称 */
    private String businessTypeName;

    /** 是否有下级节点：0无 1有 */
    private String thereSubordinateNode;

    /** 上级业务类型ID，none表示最上级 */
    private String superiorId;

    /** 下级业务类型集合 */
    private List<BusinessTypeVO> children;
}
