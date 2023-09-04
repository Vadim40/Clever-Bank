package org.CleverBank.Models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
/**
 * Модель транзакции, представляющая собой информацию о финансовой операции.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    /**
     * Уникальный идентификатор транзакции.
     */
    private int id;

    /**
     * Идентификатор исходного счета (если есть).
     */
    private int sourceAccount;

    /**
     * Идентификатор целевого счета (если есть).
     */
    private int targetAccount;

    /**
     * Тип транзакции (например, Пополнение, Снятие, Перевод и т. д.).
     */
    private TransactionType type;

    /**
     * Сумма транзакции.
     */
    private double amount;

    /**
     * Дата и время выполнения транзакции.
     */
    private LocalDate date;
}
