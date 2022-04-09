package com.games.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Transactional
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Retailer {
    @Id
    private String retailId;
    private String username;
    private double balance;
    private double profitPercentage;
    private String includeNumbers;
    @UpdateTimestamp
    private LocalDateTime creationTime;
}
