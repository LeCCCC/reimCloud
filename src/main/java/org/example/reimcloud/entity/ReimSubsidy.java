package org.example.reimcloud.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimSubsidy {

    private String id;
    private String mainId;
    private String travelerId;
    private String travelerNo;
    private String travelerName;
    private LocalDate departureDate;
    private LocalDate arrivalDate;
    private Integer subsidyDays;
    private String departureCity;
    private String departureCityNo;
    private String arrivingCity;
    private String arrivingCityNo;
    private BigDecimal applicationAmount;
    private BigDecimal subsidyAmount;
    private BigDecimal mealAllowance;
    private BigDecimal transportationAllowance;
    private BigDecimal phoneAllowance;
    private String businessTypeId;
    private String businessTypeNo;
    private String businessTypeName;
}
