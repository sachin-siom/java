package com.games.payload;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DrawOpenResponse {
    //[9057.674]
    private Map<String,List<Integer>> winnerNumber;
    //0745
    private String drawTime;
    private String date;
}
