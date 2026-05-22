package org.example.reimcloud.entity;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimItinerary {

    private String id;
    private String mainId;
    private String subsidyId;
    private String travelerId;
    private String travelerNo;
    private String travelerName;
    private LocalDate departureDate;
    private LocalDate arrivalDate;
    private String departureCity;
    private String departureCityNo;
    private String arrivingCity;
    private String arrivingCityNo;
    private String itineraryInstructions;
}
