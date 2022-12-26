package com.games.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketDetails {
  private String ticketId;
  private Map<Integer, Double> betNumber;
  private String retailId;
  private double totalWinningAmount; // if all number included
}
