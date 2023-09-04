package org.CleverBank.Models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Модель аккаунта, представляющая собой информацию о банковском счете.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {
    /**
     * Уникальный идентификатор аккаунта.
     */
    private int id;

    /**
     * Номер банковского счета.
     */
    private String accountNumber;

    /**
     * Дата создания аккаунта.
     */
    private LocalDate date;

    /**
     * Идентификатор пользователя, которому принадлежит аккаунт.
     */
    private int userId;

    /**
     * Идентификатор банка, к которому привязан аккаунт.
     */
    private int bankId;

    /**
     * Баланс аккаунта.
     */
    private double balance;

    /**
     * Дата последнего начисления процентов или выплаты по аккаунту.
     */
    private LocalDate lastInterestDate ;

    /**
     * Уменьшает баланс аккаунта на указанную сумму.
     *
     * @param amount сумма, на которую следует уменьшить баланс.
     */
    public void decreaseBalance(double amount){
        balance -= amount;
    }

    /**
     * Увеличивает баланс аккаунта на указанную сумму.
     *
     * @param amount сумма, на которую следует увеличить баланс.
     */
    public void increaseBalance(double amount){
        balance += amount;
    }
}
