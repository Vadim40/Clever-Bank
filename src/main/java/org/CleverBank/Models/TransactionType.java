package org.CleverBank.Models;

/**
 * Перечисление, представляющее типы транзакций.
 */
public enum TransactionType {
    /**
     * Тип транзакции: Пополнение.
     */
    DEPOSIT("Пополнение"),

    /**
     * Тип транзакции: Снятие.
     */
    WITHDRAWAL("Снятие"),

    /**
     * Тип транзакции: Перевод с отправителя.
     */
    TRANSFER_OUT("Перевод с отправителя"),

    /**
     * Тип транзакции: Перевод на получателя.
     */
    TRANSFER_IN("Перевод на получателя");

    private final String description;

    /**
     * Конструктор для типа транзакции с описанием.
     *
     * @param description описание типа транзакции.
     */
    TransactionType(String description) {
        this.description = description;
    }

    /**
     * Получить описание типа транзакции.
     *
     * @return описание типа транзакции.
     */
    public String getDescription() {
        return description;
    }
}
