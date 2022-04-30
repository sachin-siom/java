package com.games.payload;

import lombok.Data;

@Data
public class CommissionResponse {
    private String retailId;
    private double totalPointsPlayed;
    private double pointsWon;
    private int commissionPercentage;
    private double commission;
    private double adminProfit;
}
