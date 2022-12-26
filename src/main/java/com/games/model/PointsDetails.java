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
    private Integer isWinner; // 1-> winner 0-losser
    private Integer isClaimed; // 0 -> not claimed  1-claimed
    private LocalDateTime isClaimedTime;
    @Column(columnDefinition = "LONGTEXT")
    private String winningPoints;
    private double totalPoints; // ticket price[
    private boolean isPrinted;
    private boolean isDeleted;
    @CreationTimestamp
    private LocalDateTime creationTime;
}
