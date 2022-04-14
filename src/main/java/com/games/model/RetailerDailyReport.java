package com.games.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table( name = "retailer_daily_report", indexes = {@Index(name = "date_retailerid_index",columnList = "retailId,date" , unique = true)})
public class RetailerDailyReport {
    @Id
    @GenericGenerator(
            name = "daily-report-sequence-generator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "daily_report_sequence"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            })
    @GeneratedValue(generator = "daily-report-sequence-generator")
    private int id;
    private String retailId;
    private double playAmount;
    private double winAmount;
    private double commission;
    private double commissionAmt;
    private LocalDate date;
}
