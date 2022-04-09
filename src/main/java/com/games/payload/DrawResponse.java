package com.games.payload;

import lombok.Data;

import java.util.List;

@Data
public class DrawResponse {
    //[9057.674]
    private List<Integer> winnerNumber;
    //0745
    private String drawTime;
    private String date;
}
