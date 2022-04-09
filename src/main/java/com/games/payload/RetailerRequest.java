package com.games.payload;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RetailerRequest {

    @NotNull@NotBlank
    private String username;
    @NotNull@NotBlank
    private String password;
}
