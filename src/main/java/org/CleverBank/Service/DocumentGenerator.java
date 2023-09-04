package org.CleverBank.Service;

import org.CleverBank.Models.Account;
import org.CleverBank.Models.Transaction;
import org.CleverBank.Models.TransactionType;
import org.CleverBank.Models.User;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DocumentGenerator {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Генерирует банковский чек для операции перевода средств между счетами.
     *
     * @param amount             Сумма перевода.
     * @param operationType      Тип операции (например, "Transfer").
     * @param sourceBank         Название банка отправителя.
     * @param sourceAccountNumber Номер счета отправителя.
     * @param targetBank         Название банка получателя.
     * @param targetAccountNumber Номер счета получателя.
     */
    public void generateTransferCheck(double amount, String operationType, String sourceBank,
                                      String sourceAccountNumber, String targetBank, String targetAccountNumber) {
        // Создаем строку с содержимым чека
        String checkContent = "      Банковский чек      " + "\n" +
                "Тип операции: " + operationType + "\n" +
                "Дата и время: " + LocalDateTime.now().format(formatter) + "\n" +
                "Банк отправителя: " + sourceBank + "\n" +
                "Банк получателя: " + targetBank + "\n" +
                "Номер счета отправителя: " + sourceAccountNumber + "\n" +
                "Номер счета получателя: " + targetAccountNumber + "\n" +
                "Сумма: " + amount + " рублей\n";

        // Сохраняем чек в файл
        saveCheckToFile(checkContent);
    }

    /**
     * Генерирует банковский чек для операции депозита или снятия средств.
     *
     * @param amount        Сумма операции.
     * @param operationType Тип операции (например, "Deposit" или "Withdrawal").
     * @param bank          Название банка.
     * @param accountNumber Номер счета.
     */
    public void generateCheck(double amount, String operationType, String bank, String accountNumber) {
        // Создаем строку с содержимым чека
        String checkContent = "      Банковский чек      " + "\n" +
                "Дата и время: " + LocalDateTime.now().format(formatter) + "\n" +
                "Тип операции: " + operationType + "\n" +
                "Банк  " + bank + "\n" +
                "Номер счета : " + accountNumber + "\n" +
                "Сумма: " + amount + " рублей\n";

        // Сохраняем чек в файл
        saveCheckToFile(checkContent);
    }

    /**
     * Генерирует информацию о транзакции и возвращает ее в виде строки.
     *
     * @param transaction Транзакция.
     * @param user        Пользователь, связанный с транзакцией.
     * @return Строка с информацией о транзакции.
     */
    public StringBuilder generateInfoAboutTransaction(Transaction transaction, User user) {
        StringBuilder transactionInfo = new StringBuilder();
        transactionInfo.append("Дата: ").append(transaction.getDate()).append("  |");

        // Определяем тип операции и формируем строку информации о ней
        if (transaction.getType().equals(TransactionType.DEPOSIT)
                || transaction.getType().equals(TransactionType.WITHDRAWAL)) {
            transactionInfo.append("Тип операции: ").append(transaction.getType()).append("\t\t|");
        } else if (transaction.getType().equals(TransactionType.TRANSFER_IN)) {
            transactionInfo.append("Тип операции: ").append(transaction.getType()).append(" от ")
                    .append(user.getLastname()).append("  |");
        } else {
            transactionInfo.append("Тип операции: ").append(transaction.getType()).append(" к ")
                    .append(user.getLastname()).append("  |");
        }

        transactionInfo.append("Сумма: ").append(transaction.getAmount()).append(" рублей\n");

        return transactionInfo;
    }

    /**
     * Генерирует выписку о транзакциях для конкретного пользователя и его счета.
     *
     * @param user         Пользователь.
     * @param account      Счет пользователя.
     * @param transactions Список транзакций.
     * @return Строка с выпиской о транзакциях.
     */
    public StringBuilder generateTransactionStatement(User user, Account account, List<StringBuilder> transactions) {
        StringBuilder statementContent = new StringBuilder();
        statementContent.append("Клиент: ").append(user.getFirstname()).append(" ").
                append(user.getLastname()).append("\n");
        statementContent.append("Cчет: ").append(account.getAccountNumber()).append("\n");
        statementContent.append("Дата основания: ").append(account.getDate()).append("\n");
        statementContent.append("Дата выписки: ").append(LocalDateTime.now().format(formatter)).append("\n\n");

        statementContent.append("\t Дата \t|\t Примечание \t\t\t| Сумма\n");

        // Добавляем информацию о транзакциях
        for (StringBuilder transaction : transactions) {
            statementContent.append(transaction).append("\n");
        }

        return statementContent;
    }

    // Приватный метод для сохранения чека в файл
    private void saveCheckToFile(String checkContent) {
        try {
            FileWriter writer = new FileWriter("src/main/resources/check/check.txt", true);
            writer.write(checkContent);
            writer.write("\n\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
