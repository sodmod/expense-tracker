package org.badmus.smartexpensetrancker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Expense {
  private double amount;
  private String category;
  private LocalDate date;
  private String description;

  @Override
  public String toString() {
    return "Category: " + category +
      ", Description: " + description +
      ", Amount: " + amount +
      ", Date: " + date+"\n";
  }

}
