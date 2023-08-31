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
public class Account {
    private int id;
    private String accountNumber;
    private LocalDate date;
    private int userId;
    private int bankId;
    private double balance;
    private LocalDate lastInterestDate ;

    public void decreaseBalance(double amount){
        balance-=amount;
    }
    public void increaseBalance(double amount){
        balance+=amount;
    }
}
