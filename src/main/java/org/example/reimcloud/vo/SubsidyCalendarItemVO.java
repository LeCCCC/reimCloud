package org.example.reimcloud.vo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubsidyCalendarItemVO {

    /** 补助日历明细ID */
    private String calendarId;

    /** 出差日期 yyyy-MM-dd */
    private String travelDate;

    /** 星期 */
    private String weekName;

    /** 补助城市 */
    private String subsidyCityName;

    /** 城市类型：1一线 2二线 3三线 */
    private String cityType;

    /** 餐费补助是否选中 */
    private Boolean mealSelected;

    /** 餐费补助标准金额（一线100 二线80 三线50） */
    private BigDecimal mealStandard;

    /** 餐费补助申请金额，不得大于标准金额 */
    private BigDecimal mealAmount;

    /** 交通补助是否选中 */
    private Boolean transportSelected;

    /** 交通补助标准金额 40元/天 */
    private BigDecimal transportStandard;

    /** 交通补助申请金额，不得大于标准金额 */
    private BigDecimal transportAmount;

    /** 通讯补助是否选中 */
    private Boolean phoneSelected;

    /** 通讯补助标准金额 40元/天 */
    private BigDecimal phoneStandard;

    /** 通讯补助申请金额，不得大于标准金额 */
    private BigDecimal phoneAmount;
}
