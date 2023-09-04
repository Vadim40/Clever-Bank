package org.CleverBank.Service;

import lombok.Setter;
import org.CleverBank.Models.Account;
import org.CleverBank.Repository.AccountRepository;
import org.yaml.snakeyaml.Yaml;


import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Класс `AccountService` предоставляет методы для управления банковскими счетами и выполнения операций,
 * таких как депозиты, снятия, переводы и расчет процентов.
 */
@Setter
public class AccountService {
    // Поля класса

    /**
     * Репозиторий счетов для управления данными счетов.
     */
    private AccountRepository accountRepository;

    /**
     * Сервис транзакций для обработки операций счетов.
     */
    private TransactionService transactionService;

    /**
     * Источник данных для подключения к базе данных.
     */
    private DataSource dataSource;

    /**
     * Планировщик для запуска периодических задач.
     */
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Пул потоков для вычисления процентов по счетам.
     */
    private ExecutorService interestExecutor = Executors.newFixedThreadPool(10);

    // Конструктор

    /**
     * Конструктор класса `AccountService`, инициализирующий его с использованием источника данных.
     *
     * @param dataSource Источник данных для подключения к базе данных.
     */
    public AccountService(DataSource dataSource) {
        accountRepository = new AccountRepository(dataSource);
        transactionService = new TransactionService(dataSource);
        this.dataSource = dataSource;
    }

    // Методы для операций счетов

    /**
     * Метод для внесения средств на счет.
     *
     * @param account Счет, на который вносятся средства.
     * @param amount  Сумма для внесения на счет.
     * @throws IllegalArgumentException Если сумма отрицательная.
     */

    public void deposit(Account account, double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Refill amount must be positive");
        }
        account.increaseBalance(amount);
        accountRepository.updateAccountById(account, account.getId());
        transactionService.saveDepositTransfer(account, amount);
    }

    /**
     * Метод для снятия средств со счета.
     *
     * @param account Счет, с которого снимаются средства.
     * @param amount  Сумма для снятия со счета.
     * @throws IllegalArgumentException Если на счету недостаточно средств.
     */
    public void withdraw(Account account, double amount) {
        if (account.getBalance() - amount < 0) {
            throw new IllegalArgumentException("Insufficient funds to withdraw");
        }
        account.decreaseBalance(amount);
        accountRepository.updateAccountById(account, account.getId());
        transactionService.saveWithdrawTransfer(account, amount);
    }

    /**
     * Метод для выполнения перевода средств между счетами.
     *
     * @param sourceAccount Счет-источник средств.
     * @param targetAccount Счет-получатель средств.
     * @param amount        Сумма для перевода.
     * @throws IllegalArgumentException Если на счете-источнике недостаточно средств.
     */
    public void transfer(Account sourceAccount, Account targetAccount, double amount) {
        if (sourceAccount.getBalance() - amount < 0) {
            throw new IllegalArgumentException("Not enough funds");
        }

        Account firstLockedAccount = sourceAccount.getId() < targetAccount.getId() ? sourceAccount : targetAccount;
        Account secondLockedAccount = sourceAccount.getId() < targetAccount.getId() ? targetAccount : sourceAccount;

        synchronized (firstLockedAccount) {
            synchronized (secondLockedAccount) {
                try (Connection connection = dataSource.getConnection()) {
                    connection.setAutoCommit(false);

                    sourceAccount.decreaseBalance(amount);
                    accountRepository.updateAccountById(sourceAccount, sourceAccount.getId());

                    targetAccount.increaseBalance(amount);
                    accountRepository.updateAccountById(targetAccount, targetAccount.getId());

                    transactionService.createTransferTransactions(sourceAccount, targetAccount, amount);
                    connection.commit();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Метод для запуска расчета и применения процентов к счетам.
     */
    public void startInterestCalculation() {
        // Создаем задачу, выполняющуюся периодически
        Runnable interestTask = () -> {
            LocalDate now = LocalDate.now();
            List<Account> accounts = accountRepository.getAllAccounts();

            for (Account account : accounts) {
                LocalDate lastInterestDate = account.getLastInterestDate();
                // Проверяем, нужно ли начислить проценты
                if (lastInterestDate == null || lastInterestDate.until(now, ChronoUnit.MONTHS) >= 1) {
                    // Выполняем расчет и начисление процентов асинхронно
                    interestExecutor.submit(() -> calculateAndApplyInterest(account));
                }
            }
        };

        // Запускаем задачу с фиксированным интервалом
        scheduler.scheduleAtFixedRate(interestTask, 0, 30, TimeUnit.SECONDS);
    }

    /**
     * Приватный метод для расчета и начисления процентов на счет.
     *
     * @param account Счет, на который начисляются проценты.
     */
    private void calculateAndApplyInterest(Account account) {
        double balance = account.getBalance();
        Yaml yaml = new Yaml();
        InputStream inputStream = getClass().getResourceAsStream("/config.yml");

        Map<String, Double> config = yaml.load(inputStream);
        double interest = config.get("interestRate");

        // Увеличиваем баланс счета на начисленные проценты
        account.increaseBalance(interest);
        account.setLastInterestDate(LocalDate.now());
        accountRepository.updateAccountById(account, account.getId());
    }

    /**
     * Метод для остановки расчета процентов.
     */
    public void stopInterestCalculation() {
        // Останавливаем планировщик и пул потоков для расчета процентов
        scheduler.shutdown();
        interestExecutor.shutdown();
    }

    /**
     * Метод для получения счета по его идентификатору.
     *
     * @param accountId Идентификатор счета.
     * @return Счет с указанным идентификатором.
     * @throws RuntimeException Если счет не найден.
     */
    public Account getAccount(int accountId) {
        Account account = accountRepository.getAccountById(accountId);
        if (account != null) {
            return account;
        } else {
            throw new RuntimeException("Account not found");
        }
    }

    /**
     * Метод для получения списка всех счетов.
     *
     * @return Список всех счетов.
     */
    public List<Account> getAllAccounts() {
        return accountRepository.getAllAccounts();
    }

    /**
     * Метод для сохранения счета.
     *
     * @param account Счет для сохранения.
     * @return Сохраненный счет.
     */
    public Account saveAccount(Account account) {
        account.setDate(LocalDate.now());
        return accountRepository.saveAccount(account);
    }

    /**
     * Метод для обновления счета по его идентификатору.
     *
     * @param account   Счет для обновления.
     * @param accountId Идентификатор счета.
     */
    public void updateAccount(Account account, int accountId) {
        accountRepository.updateAccountById(account, accountId);
    }

    /**
     * Метод для удаления счета по его идентификатору.
     *
     * @param accountId Идентификатор счета для удаления.
     * @throws RuntimeException Если счет не найден.
     */
    public void deleteAccount(int accountId) {
        if (accountRepository.getAccountById(accountId) != null) {
            accountRepository.deleteAccountById(accountId);
        } else {
            throw new RuntimeException("Account not found");
        }
    }
}
