package com.games.payload;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PointPlayRequest {
    @NotNull
    private List<Points> pointArrays;
    @NotBlank
    @NotNull
    private String retailId;

    private String drawTime;
}

