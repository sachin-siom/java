package com.games.model;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class LoadResponse {
  private double totalCollectionAmt;
  private Map<Integer, Load> loadData;
}
