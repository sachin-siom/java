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
@Table( name = "points_details", indexes = {@Index(name = "drawTime_creationTime_index",columnList = "drawTime,creationTime"),
        @Index(name = "retailid_creationTime_index",columnList = "retailId,creationTime")})
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
    private boolean isPrinted;
    @CreationTimestamp
    private LocalDateTime creationTime;
}
