package com.games.payload;

import lombok.Data;

import javax.persistence.Entity;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PointPlayRequest {
    @NotNull
    private List<Points> pointArrays;
    @NotBlank(message = "retailId can not be blank")
    @NotNull(message = "retailId can not be null")
    private String retailId;
    @NotBlank(message = "drawtime can not be blank")
    @NotNull(message = "drawtime can not be null")
    private String drawTime;
}

