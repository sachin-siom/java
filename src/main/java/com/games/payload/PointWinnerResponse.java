package com.games.payload;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PointWinnerResponse {
    private String points;
    private String ticketId;
    private String drawTime;
    private String retailId;
    private String winningPoints;
    private String ticketTime;
    private boolean isWinner;
    private boolean isClaimed;
    private String claimTime;
    private double totalTicketValue;
    private boolean isDeleted;
    private boolean isPrinted;
}
