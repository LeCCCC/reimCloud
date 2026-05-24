package org.example.reimcloud.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripVO {

    /** 补录行程ID */
    private String tripId;

    /** 出行人ID */
    private String travelerId;

    /** 出行人姓名 */
    private String travelerName;

    /** 出差日期范围 yyyy-MM-dd至yyyy-MM-dd */
    private String tripDateRange;

    /** 行程（出发城市-到达城市） */
    private String tripRoute;

    /** 行程说明 */
    private String tripDescription;

    /** 关联生成的补助信息ID */
    private String subsidyId;
}
