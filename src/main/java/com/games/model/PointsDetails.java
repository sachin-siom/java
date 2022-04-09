package com.games.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class PointsDetails {
    @Id
    private String ticketId;
    private String retailId;
    private String drawTime;
    @Column(columnDefinition = "LONGTEXT")
    private String points;
    private Integer isWinner;
    private Integer isClaimed;
    private LocalDateTime isClaimedTime;
    @Column(columnDefinition = "LONGTEXT")
    private String winningPoints;
    @CreationTimestamp
    private LocalDateTime creationTime;
}
