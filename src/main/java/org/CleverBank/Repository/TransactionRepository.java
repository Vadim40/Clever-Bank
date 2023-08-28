package org.CleverBank.Repository;


import org.CleverBank.Models.Transaction;

import javax.sql.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {

    private DataSource dataSource;

    public TransactionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Transaction getTransactionById(int transactionId) {
        String sql = "SELECT * FROM transactions WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, transactionId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapTransactionFromResultSet(resultSet);
                } else {
                   return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public List<Transaction> getAllTransactions() {
        String sql = "SELECT * FROM transactions";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Transaction> transactions = new ArrayList<>();
                while (resultSet.next()) {
                    transactions.add((mapTransactionFromResultSet(resultSet)));
                }
                return transactions;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get transactions", e);
        }
    }

    public Transaction saveTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (source_account,target_account,amount,transaction_date) VALUES (?,?,?,?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, transaction.getSourceAccount());
            preparedStatement.setInt(2, transaction.getTargetAccount());
            preparedStatement.setInt(3, transaction.getAmount());
            preparedStatement.setDate(4, Date.valueOf(transaction.getDate()));
            preparedStatement.executeUpdate();
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    transaction.setId(generatedId);
                } else {
                    throw new RuntimeException("Failed to get generated transaction id");
                }
            }
            return transaction;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create transaction", e);
        }
    }

    public void updateTransactionById(Transaction transaction, int transactionId) {
        String sql = "UPDATE transactions SET source_account = ?, target_account = ?," +
                " amount = ?, transaction_date = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, transaction.getSourceAccount());
            preparedStatement.setInt(2, transaction.getTargetAccount());
            preparedStatement.setInt(3, transaction.getAmount());
            preparedStatement.setDate(4, Date.valueOf(transaction.getDate()));
            preparedStatement.setInt(5, transactionId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update transaction ", e);
        }
    }

    public void deleteTransactionById(int transactionId) {
        String sql = "DELETE FROM transactions WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, transactionId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete transaction", e);
        }
    }

    private Transaction mapTransactionFromResultSet(ResultSet resultSet) throws SQLException {
        return new Transaction(
                resultSet.getInt("id"),
                resultSet.getInt("source_account"),
                resultSet.getInt("target_account"),
                resultSet.getInt("amount"),
                resultSet.getDate("transaction_date").toLocalDate()
        );
    }
}
