package org.CleverBank.Service;

import org.CleverBank.Models.Account;
import org.CleverBank.Models.Transaction;
import org.CleverBank.Models.TransactionType;
import org.CleverBank.Repository.TransactionRepository;

import javax.sql.DataSource;
import java.time.LocalDate;

public class TransactionService {
    private TransactionRepository transactionRepository;


    public TransactionService(DataSource dataSource) {
        this.transactionRepository = new TransactionRepository(dataSource);
    }

    public Transaction getTransaction(int transactionId) {
        Transaction transaction = transactionRepository.getTransactionById(transactionId);
        if (transaction != null) {
            return transaction;
        } else {
            throw new RuntimeException("Transaction not found");
        }
    }

    public Transaction saveTransaction(Transaction transaction) {
        transaction.setDate(LocalDate.now());
        return transactionRepository.saveTransaction(transaction);
    }

    public void updateTransaction(Transaction transaction, int transactionId) {
        transactionRepository.updateTransactionById(transaction, transactionId);
    }

    public void deleteTransaction(int transactionId) {
        if (transactionRepository.getTransactionById(transactionId) != null) {
            transactionRepository.deleteTransactionById(transactionId);
        } else {
            throw new RuntimeException("Transaction not found");
        }
    }
    public void createTransferTransactions(Account sourceAccount, Account targetAccount, double amount) {
        Transaction depositTransaction=Transaction.builder()
                .amount(amount)
                .sourceAccount(sourceAccount.getId())
                .targetAccount(targetAccount.getId())
                .date(LocalDate.now())
                .type(TransactionType.TRANSFER_IN)
                .build();
        transactionRepository.saveTransaction(depositTransaction);

        Transaction withdrawTransaction=Transaction.builder()
                .amount(-amount)
                .sourceAccount(sourceAccount.getId())
                .targetAccount(targetAccount.getId())
                .date(LocalDate.now())
                .type(TransactionType.TRANSFER_OUT)
                .build();
        transactionRepository.saveTransaction(withdrawTransaction);
    }
    public void saveDepositTransfer(Account account, double amount) {
        Transaction depositTransaction=Transaction.builder()
                .amount(amount)
                .sourceAccount(account.getId())
                .date(LocalDate.now())
                .type(TransactionType.DEPOSIT)
                .build();
        transactionRepository.saveTransaction(depositTransaction);
    }
    public void saveWithdrawTransfer(Account account, double amount){
        Transaction withdrawTransaction=Transaction.builder()
                .amount(-amount)
                .sourceAccount(account.getId())
                .date(LocalDate.now())
                .type(TransactionType.WITHDRAWAL)
                .build();
        transactionRepository.saveTransaction(withdrawTransaction);
    }
}
