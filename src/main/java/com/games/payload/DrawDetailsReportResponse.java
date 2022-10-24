package com.games.payload;

import com.games.model.PointsDetails;
import lombok.Data;

import java.util.List;

@Data
public class DrawDetailsReportResponse {
    private int id;
    private String draw;
    private int setPoints;
    private double wonPoints;
    private int betCount;
    private int winCount;
    private List<Integer> winNumbers;
    private PointsDetails pointsDetails;
}
