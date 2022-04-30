package com.games.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RetailerAudit {
    @Id
    @GenericGenerator(
            name = "audit-sequence-generator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "audit_sequence"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            })
    @GeneratedValue(generator = "audit-sequence-generator")
    private int id;
    private String retailId;
    private double balance; // current balance
    private double amount; // txn amount
    private int isCredit; // 0 -> debit from retailer account, 1 -> credit into retailer account
    private int creditor; // 1 admin, 2 player
    private String ticketId;
    @CreationTimestamp
    private LocalDateTime creationTime;

}
