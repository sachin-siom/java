package com.games.payload;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class Points {

    @NotNull(message = "points can not be null")
    private Map<Integer, Integer> points;

    @NotNull(message = "winningMultiplier can not be blank")
    private Integer winningMultiplier;

    @NotNull(message = "pricePerPoint can not be blank")
    private Integer pricePerPoint;
}
