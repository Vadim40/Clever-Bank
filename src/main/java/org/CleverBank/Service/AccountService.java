package org.CleverBank.Service;

import org.CleverBank.Models.Account;
import org.CleverBank.Repository.AccountRepository;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AccountService {
    private AccountRepository accountRepository;
    private TransactionService transactionService;
    private DataSource dataSource;
    private  ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private  ExecutorService interestExecutor = Executors.newFixedThreadPool(10);


    public AccountService(DataSource dataSource) {
        accountRepository = new AccountRepository(dataSource);
        transactionService = new TransactionService(dataSource);
        this.dataSource = dataSource;
    }

    public void deposit(Account account, double amount) {

        if (amount < 0) {
            throw new IllegalArgumentException("Refill amount must be positive");
        }

        account.increaseBalance(amount);
        accountRepository.updateAccountById(account, account.getId());

        transactionService.saveDepositTransfer(account, amount);
    }


    public void withdraw(Account account, double amount) {

        if (account.getBalance() - amount < 0) {
            throw new IllegalArgumentException("Insufficient funds to withdraw");
        }

        account.decreaseBalance(amount);
        accountRepository.updateAccountById(account, account.getId());

        transactionService.saveWithdrawTransfer(account, amount);
    }

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
    public void startInterestCalculation() {
        Runnable interestTask = () -> {
            LocalDate now = LocalDate.now();
            List<Account> accounts = accountRepository.getAllAccounts();

            for (Account account : accounts) {
                LocalDate lastInterestDate = account.getLastInterestDate();
                if (lastInterestDate == null || lastInterestDate.until(now, ChronoUnit.MONTHS) >= 1) {
                    interestExecutor.submit(() -> calculateAndApplyInterest(account));
                }
            }
        };

        scheduler.scheduleAtFixedRate(interestTask, 0, 30, TimeUnit.SECONDS);
    }

    private void calculateAndApplyInterest(Account account) {
        double balance = account.getBalance();
        double interest = balance*0.01;
        account.increaseBalance(interest);
        account.setLastInterestDate(LocalDate.now());
        accountRepository.updateAccountById(account, account.getId());
    }

    public void stopInterestCalculation() {
        scheduler.shutdown();
        interestExecutor.shutdown();
    }


    public Account getAccount(int accountId) {
        Account account = accountRepository.getAccountById(accountId);
        if (account != null) {
            return account;
        } else {
            throw new RuntimeException("Account not found");
        }
    }

    public Account saveAccount(Account account) {
        account.setDate(LocalDate.now());
        return accountRepository.saveAccount(account);
    }

    public void updateAccount(Account account, int accountId) {
        accountRepository.updateAccountById(account, accountId);
    }

    public void deleteAccount(int accountId) {
        if (accountRepository.getAccountById(accountId) != null) {
            accountRepository.deleteAccountById(accountId);
        } else {
            throw new RuntimeException("Account not found");
        }
    }
}
