package com.games.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Load {
  private int number;
  private int betCount;
  private double betAmount;
}
