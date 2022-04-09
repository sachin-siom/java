package com.games.payload;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class Points {
    @NotBlank
    @NotNull
    private Map<Integer, Integer> points;
    @NotBlank
    @NotNull
    private Integer winningMultiplier;
    @NotBlank
    @NotNull
    private Integer pricePerPoint;
}
