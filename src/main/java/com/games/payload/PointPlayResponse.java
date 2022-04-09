package com.games.payload;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PointPlayResponse {
    private String points;
    private String ticketId;
    private String drawTime;
    private LocalDateTime currentTime;
}
