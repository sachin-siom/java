package com.games.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WinningDetails {
    Map<Integer, Double> winningNums = new HashMap<>();
}
