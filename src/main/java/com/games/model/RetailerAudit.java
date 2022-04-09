package com.games.model;

import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
public class RetailerAudit {
    @Id
    @GeneratedValue
    private int id;
    private String retailId;
    private double balance; // current balance
    private double amount; // txn amount
    private int isCredit;
    private int creditor; // 1 admin, 2 player
    private String ticketId;
    @CreationTimestamp
    private LocalDateTime creationTime;

}
