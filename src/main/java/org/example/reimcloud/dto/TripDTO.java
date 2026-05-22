package org.example.reimcloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDTO {

    /** 出行人ID */
    private String travelerId;

    /** 出行人工号 */
    private String travelerNo;

    /** 出行人姓名 */
    private String travelerName;

    /** 出发城市编号 */
    private String departureCityNo;

    /** 出发城市名称 */
    private String departureCityName;

    /** 到达城市编号 */
    private String arrivalCityNo;

    /** 到达城市名称 */
    private String arrivalCityName;

    /** 出发日期 yyyy-MM-dd */
    private String departureDate;

    /** 到达日期，不可早于出发日期且不可晚于当前日期 yyyy-MM-dd */
    private String arrivalDate;

    /** 行程说明，最大500字 */
    private String tripDescription;
}
