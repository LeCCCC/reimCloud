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
public class SubsidyCalendar {

    private String id;
    private String subsidyId;
    private LocalDate travelDate;
    private String travelDateWeek;
    private String subsidizedCities;
    private String subsidizedCityNumber;
    private String cityType;
    private BigDecimal standardMealExpensesAmount;
    private BigDecimal standardTrafficAmount;
    private BigDecimal standardCommunicationAmount;
    private BigDecimal mealExpensesAmount;
    private BigDecimal trafficAmount;
    private BigDecimal communicationAmount;
    private String mealSelected;
    private String trafficSelected;
    private String communicationSelected;
}
