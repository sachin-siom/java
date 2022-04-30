package com.games.payload;

import lombok.Data;

import java.util.List;

@Data
public class CommissionReportResponse {
    private List<CommissionResponse> commissionResponseList;
    private double totalPointsPlayed;
    private double totalPointsWon;
    private double totalCommission;
    private double totalProfit;
}
