package com.games.payload;

import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Builder
public class RetailerResponse {

    private String retailId;
    private String username;
    private double balance;
    private boolean isAdmin;
    private boolean status;
    private String macAddress;
    private String includeNumbers;
    private String profitPercentage;
    private LocalDateTime creationTime;
}
