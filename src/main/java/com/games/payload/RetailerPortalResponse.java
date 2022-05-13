package com.games.payload;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RetailerPortalResponse {

    private String retailId;
    private String username;
    private double balance;
    private boolean isAdmin;
    private String status;
    private String macAddress;
    private String includeNumbers;
    private String profitPercentage;
    private LocalDateTime creationTime;
}
