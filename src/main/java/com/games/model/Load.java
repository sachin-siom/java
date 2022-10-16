package com.games.model;

import com.games.payload.DrawResponse;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Load implements Comparable<Load>{
  private Integer number;
  private int betCount;
  private double betAmount;

  @Override
  public int compareTo(Load o) {
    if (Objects.isNull(o)) {
      return 0;
    }
    return getNumber().compareTo(o.getNumber());
  }

}
