package org.CleverBank.Models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    private int id;
    private int sourceAccount;
    private int targetAccount;
    private int amount;
    private LocalDate date;
}
