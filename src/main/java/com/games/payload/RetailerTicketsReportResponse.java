package com.games.payload;

import lombok.Data;

@Data
public class RetailerTicketsReportResponse {
    private int id;
    private String draw;
    private String retailerId;
    private String ticketid;
    private int setPoints;
    private double wonPoints;
    private String claimed;
    private String claimedTime;
}
