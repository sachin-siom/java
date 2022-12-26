package com.games.model;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BetDetail {
  private Double winningAmt;
  private Set<String> retailId;
  private Set<String> ticketId;
}
