package com.games.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LastXTxn {
    private int no;
    private Double amount;
    private LocalDateTime date;
}
