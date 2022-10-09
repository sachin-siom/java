package com.games.payload;

import java.util.Objects;
import lombok.Data;

import java.util.List;

@Data
public class DrawResponse implements Comparable<DrawResponse>{
    //[9057.674]
    private List<Integer> winnerNumber;
    //0745
    private String drawTime;
    private String date;

    @Override
    public int compareTo(DrawResponse o) {
        if (Objects.isNull(o)) {
            return 0;
        }
        return getDrawTime().compareTo(o.getDrawTime());
    }
}
