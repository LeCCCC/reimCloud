package org.example.reimcloud.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityVO {

    /** 城市编号 */
    private String cityNo;

    /** 城市名称 */
    private String cityName;

    /** 城市类型：1一线 2二线 3三线 */
    private String cityType;
}
