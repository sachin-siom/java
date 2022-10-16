package com.games.model;

import com.games.payload.DrawResponse;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Load implements Comparable<Load>{
  private Integer number;
  private int betCount;
  private double betAmount;
  private List<String> betTicketIds;
  private boolean isWinner;

  @Override
  public int compareTo(Load o) {
    if (Objects.isNull(o)) {
      return 0;
    }
    return getNumber().compareTo(o.getNumber());
  }

}
