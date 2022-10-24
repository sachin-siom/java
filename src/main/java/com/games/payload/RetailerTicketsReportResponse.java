package com.games.payload;

import com.games.model.PointsDetails;
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
    private PointsDetails pointDetails;
}
