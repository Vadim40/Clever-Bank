package org.CleverBank.Service;

import lombok.Setter;
import org.CleverBank.Models.Account;
import org.CleverBank.Models.Transaction;
import org.CleverBank.Models.TransactionType;
import org.CleverBank.Repository.BankRepository;
import org.CleverBank.Repository.TransactionRepository;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;/**
 * Сервисный класс `TransactionService` предоставляет функциональность для работы с транзакциями,
 * создания транзакций и генерации чеков.
 */
@Setter
public class TransactionService {

    private TransactionRepository transactionRepository;
    private BankRepository bankRepository;
    private DocumentGenerator documentGenerator = new DocumentGenerator();

    /**
     * Конструктор класса `TransactionService`.
     *
     * @param dataSource Источник данных (DataSource), используемый для взаимодействия с базой данных.
     */
    public TransactionService(DataSource dataSource) {
        transactionRepository = new TransactionRepository(dataSource);
        bankRepository = new BankRepository(dataSource);
    }

    /**
     * Получает транзакцию по идентификатору.
     *
     * @param transactionId Идентификатор транзакции.
     * @return Объект транзакции.
     * @throws RuntimeException, если транзакция не найдена.
     */
    public Transaction getTransaction(int transactionId) {
        Transaction transaction = transactionRepository.getTransactionById(transactionId);
        if (transaction != null) {
            return transaction;
        } else {
            throw new RuntimeException("Transaction not found");
        }
    }

    /**
     * Получает список всех транзакций.
     *
     * @return Список всех транзакций.
     */
    public List<Transaction> getAllTransaction() {
        return transactionRepository.getAllTransactions();
    }

    /**
     * Получает список всех транзакций для определенного счета и за указанный период времени.
     *
     * @param accountId  Идентификатор счета.
     * @param startDate  Начальная дата периода.
     * @param endDate    Конечная дата периода.
     * @return Список транзакций.
     */
    public List<Transaction> getAllTransactionByAccountAndTime(int accountId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getAllTransactionsByAccountIdAndTime(accountId, startDate, endDate);
    }

    /**
     * Сохраняет новую транзакцию и устанавливает текущую дату.
     *
     * @param transaction Объект транзакции для сохранения.
     * @return Сохраненный объект транзакции.
     */
    public Transaction saveTransaction(Transaction transaction) {
        transaction.setDate(LocalDate.now());
        return transactionRepository.saveTransaction(transaction);
    }

    /**
     * Обновляет информацию о транзакции.
     *
     * @param transaction   Объект транзакции с обновленными данными.
     * @param transactionId Идентификатор транзакции для обновления.
     */
    public void updateTransaction(Transaction transaction, int transactionId) {
        transactionRepository.updateTransactionById(transaction, transactionId);
    }

    /**
     * Удаляет транзакцию по идентификатору.
     *
     * @param transactionId Идентификатор транзакции для удаления.
     * @throws RuntimeException, если транзакция не найдена.
     */
    public void deleteTransaction(int transactionId) {
        if (transactionRepository.getTransactionById(transactionId) != null) {
            transactionRepository.deleteTransactionById(transactionId);
        } else {
            throw new RuntimeException("Transaction not found");
        }
    }

    /**
     * Создает две транзакции для операции перевода между счетами и генерирует чек для перевода.
     *
     * @param sourceAccount Счет-источник перевода.
     * @param targetAccount Счет-получатель перевода.
     * @param amount        Сумма перевода.
     */
    public void createTransferTransactions(Account sourceAccount, Account targetAccount, double amount) {
        Transaction depositTransaction = Transaction.builder()
                .amount(amount)
                .sourceAccount(sourceAccount.getId())
                .targetAccount(targetAccount.getId())
                .date(LocalDate.now())
                .type(TransactionType.TRANSFER_IN)
                .build();
        transactionRepository.saveTransaction(depositTransaction);

        Transaction withdrawTransaction = Transaction.builder()
                .amount(-amount)
                .sourceAccount(sourceAccount.getId())
                .targetAccount(targetAccount.getId())
                .date(LocalDate.now())
                .type(TransactionType.TRANSFER_OUT)
                .build();
        transactionRepository.saveTransaction(withdrawTransaction);

        createTransferCheck(sourceAccount, targetAccount, amount);
    }

    /**
     * Создает чек для операции перевода.
     *
     * @param sourceAccount        Счет-источник перевода.
     * @param targetAccount        Счет-получатель перевода.
     * @param amount               Сумма перевода.
     */
    private void createTransferCheck(Account sourceAccount, Account targetAccount, double amount) {
        String sourceBank = bankRepository.getBankById(sourceAccount.getBankId()).getName();
        String targetBank = bankRepository.getBankById(targetAccount.getBankId()).getName();
        String sourceAccountNumber = sourceAccount.getAccountNumber();
        String targetAccountNumber = targetAccount.getAccountNumber();

        documentGenerator.generateTransferCheck(amount, "Transfer", sourceBank,
                sourceAccountNumber, targetBank, targetAccountNumber);
    }

    /**
     * Создает чек для операции депозита.
     *
     * @param account Счет, на который выполняется депозит.
     * @param amount  Сумма депозита.
     */
    public void saveDepositTransfer(Account account, double amount) {
        Transaction depositTransaction = Transaction.builder()
                .amount(amount)
                .sourceAccount(account.getId())
                .targetAccount(account.getId())
                .date(LocalDate.now())
                .type(TransactionType.DEPOSIT)
                .build();
        transactionRepository.saveTransaction(depositTransaction);

        createCheck(TransactionType.DEPOSIT.getDescription(), account, amount);
    }

    /**
     * Создает чек для операции снятия денег.
     *
     * @param account Счет, с которого производится снятие.
     * @param amount  Сумма снятия.
     */
    public void saveWithdrawTransfer(Account account, double amount) {
        Transaction withdrawTransaction = Transaction.builder()
                .amount(-amount)
                .sourceAccount(account.getId())
                .targetAccount(account.getId())
                .date(LocalDate.now())
                .type(TransactionType.WITHDRAWAL)
                .build();
        transactionRepository.saveTransaction(withdrawTransaction);

        createCheck(TransactionType.WITHDRAWAL.getDescription(), account, amount);
    }

    /**
     * Создает чек для операции.
     *
     * @param operationType Тип операции (депозит или снятие).
     * @param account       Счет, на котором произошла операция.
     * @param amount        Сумма операции.
     */
    private void createCheck(String operationType, Account account, double amount) {
        String bank = bankRepository.getBankById(account.getBankId()).getName();
        String accountNumber = account.getAccountNumber();
        documentGenerator.generateCheck(amount, operationType, bank, accountNumber);
    }
}
