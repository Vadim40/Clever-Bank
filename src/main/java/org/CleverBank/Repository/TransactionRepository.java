/**
 * Репозиторий для работы с транзакциями.
 */
package org.CleverBank.Repository;

import org.CleverBank.Models.Transaction;
import org.CleverBank.Models.TransactionType;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {

    private DataSource dataSource;

    /**
     * Конструктор класса TransactionRepository.
     *
     * @param dataSource источник данных для выполнения операций с базой данных.
     */
    public TransactionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Получить транзакцию по ее идентификатору.
     *
     * @param transactionId идентификатор транзакции.
     * @return объект транзакции, если найден, в противном случае null.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
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

    /**
     * Получить список всех транзакций.
     *
     * @return список объектов транзакций.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public List<Transaction> getAllTransactions() {
        String sql = "SELECT * FROM transactions";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Transaction> transactions = new ArrayList<>();
                while (resultSet.next()) {
                    transactions.add(mapTransactionFromResultSet(resultSet));
                }
                return transactions;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get transactions", e);
        }
    }

    /**
     * Получить список всех транзакций для указанного аккаунта в заданном временном диапазоне.
     *
     * @param accountId  идентификатор аккаунта.
     * @param startDate  начальная дата временного диапазона.
     * @param endDate    конечная дата временного диапазона.
     * @return список объектов транзакций для указанного аккаунта и временного диапазона.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public List<Transaction> getAllTransactionsByAccountIdAndTime(int accountId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM transactions WHERE source_account=? and transaction_date BETWEEN ? AND ? ";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, accountId);
            preparedStatement.setDate(2, java.sql.Date.valueOf(startDate));
            preparedStatement.setDate(3, java.sql.Date.valueOf(endDate));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Transaction> transactions = new ArrayList<>();
                while (resultSet.next()) {
                    transactions.add(mapTransactionFromResultSet(resultSet));
                }
                return transactions;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get transactions", e);
        }
    }

    /**
     * Сохранить транзакцию в базе данных.
     *
     * @param transaction объект транзакции для сохранения.
     * @return объект транзакции с установленным идентификатором.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public Transaction saveTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (source_account, target_account, amount, transaction_type, transaction_date) VALUES (?,?,?,?,?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, transaction.getSourceAccount());
            preparedStatement.setInt(2, transaction.getTargetAccount());
            preparedStatement.setDouble(3, transaction.getAmount());
            preparedStatement.setString(4, transaction.getType().name());
            Date date = Date.valueOf(transaction.getDate());
            preparedStatement.setDate(5, date);
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

    /**
     * Обновить транзакцию по ее идентификатору.
     *
     * @param transaction   объект транзакции с обновленными данными.
     * @param transactionId идентификатор транзакции, которую следует обновить.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public void updateTransactionById(Transaction transaction, int transactionId) {
        String sql = "UPDATE transactions SET source_account=?, target_account=?, " +
                "amount=?, transaction_type=?, transaction_date=? WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, transaction.getSourceAccount());
            preparedStatement.setInt(2, transaction.getTargetAccount());
            preparedStatement.setDouble(3, transaction.getAmount());
            preparedStatement.setString(4, transaction.getType().name());
            preparedStatement.setDate(5, Date.valueOf(transaction.getDate()));
            preparedStatement.setInt(6, transactionId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update transaction ", e);
        }
    }

    /**
     * Удалить транзакцию по ее идентификатору.
     *
     * @param transactionId идентификатор транзакции, которую следует удалить.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
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

    /**
     * Преобразовать результат SQL-запроса в объект транзакции.
     *
     * @param resultSet результат SQL-запроса с данными о транзакции.
     * @return объект транзакции.
     * @throws SQLException если произошла ошибка при обработке результата запроса.
     */
    private Transaction mapTransactionFromResultSet(ResultSet resultSet) throws SQLException {
        return new Transaction(
                resultSet.getInt("id"),
                resultSet.getInt("source_account"),
                resultSet.getInt("target_account"),
                TransactionType.valueOf(resultSet.getString("transaction_type")),
                resultSet.getDouble("amount"),
                resultSet.getDate("transaction_date").toLocalDate()
        );
    }
}
