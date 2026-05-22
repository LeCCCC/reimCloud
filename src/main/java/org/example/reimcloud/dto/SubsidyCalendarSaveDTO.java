package org.example.reimcloud.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubsidyCalendarSaveDTO {

    /** 补助日历明细ID */
    private String calendarId;

    /** 餐费补助是否选中 */
    private Boolean mealSelected;

    /** 餐费补助申请金额，保留2位小数 */
    private BigDecimal mealAmount;

    /** 交通补助是否选中 */
    private Boolean transportSelected;

    /** 交通补助申请金额，保留2位小数 */
    private BigDecimal transportAmount;

    /** 通讯补助是否选中 */
    private Boolean phoneSelected;

    /** 通讯补助申请金额，保留2位小数 */
    private BigDecimal phoneAmount;
}
