package org.CleverBank.Models;

public enum TransactionType {
    DEPOSIT("Пополнение"),
    WITHDRAWAL("Снятие"),
    TRANSFER_OUT("Перевод с отправителя"),
    TRANSFER_IN("Перевод на получателя");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
