package org.example.reimcloud.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectVO {

    /** 项目ID */
    private String projectId;

    /** 项目编号 */
    private String projectNo;

    /** 项目名称 */
    private String projectName;
}
