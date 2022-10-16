package com.games.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;

@Data
public class LoadResponse {
  private double totalCollectionAmt;
  private Set<Load> loadData;
}
