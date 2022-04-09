package com.games.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class WinnerPointDetails {
    @Id
    @GeneratedValue
    private Long id;
    private String drawTime;
    @Column(columnDefinition = "LONGTEXT")
    private String winnerPoint;
    @CreationTimestamp
    private LocalDate creationTime;
}
