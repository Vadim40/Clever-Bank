/**
 * Репозиторий для работы с аккаунтами.
 */
package org.CleverBank.Repository;

import org.CleverBank.Models.Account;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountRepository {

    private DataSource dataSource;

    /**
     * Конструктор класса AccountRepository.
     *
     * @param dataSource источник данных для выполнения операций с базой данных.
     */
    public AccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Получить аккаунт по его идентификатору.
     *
     * @param accountId идентификатор аккаунта.
     * @return объект аккаунта, если найден, в противном случае null.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public Account getAccountById(int accountId) {
        String sql = "SELECT * FROM account WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, accountId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAccountFromResultSet(resultSet);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get account by ID", e);
        }
    }

    /**
     * Получить список всех аккаунтов.
     *
     * @return список объектов аккаунтов.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public List<Account> getAllAccounts() {
        String sql = "SELECT * FROM account";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Account> accounts = new ArrayList<>();
                while (resultSet.next()) {
                    accounts.add(mapAccountFromResultSet(resultSet));
                }
                return accounts;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all accounts", e);
        }
    }

    /**
     * Сохранить аккаунт в базе данных.
     *
     * @param account объект аккаунта для сохранения.
     * @return объект аккаунта с установленным идентификатором.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public Account saveAccount(Account account) {
        String sql = "INSERT INTO account (account_number, account_date, user_id," +
                "bank_id, balance, last_interest_date) VALUES (?,?,?,?,?,?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, account.getAccountNumber());
            preparedStatement.setDate(2, Date.valueOf(account.getDate()));
            preparedStatement.setInt(3, account.getUserId());
            preparedStatement.setInt(4, account.getBankId());
            preparedStatement.setDouble(5, account.getBalance());
            preparedStatement.setDate(6, Date.valueOf(account.getLastInterestDate()));
            preparedStatement.executeUpdate();
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    account.setId(generatedId);
                } else {
                    throw new RuntimeException("Failed to get generated account ID");
                }
            }
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create account", e);
        }
    }

    /**
     * Обновить аккаунт по его идентификатору.
     *
     * @param account   объект аккаунта с обновленными данными.
     * @param accountId идентификатор аккаунта, который следует обновить.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public void updateAccountById(Account account, int accountId) {
        String sql = "UPDATE account SET account_number = ?, account_date=?, user_id = ?," +
                " bank_id = ?, balance = ?, last_interest_date=? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, account.getAccountNumber());
            preparedStatement.setDate(2, Date.valueOf(account.getDate()));
            preparedStatement.setInt(3, account.getUserId());
            preparedStatement.setInt(4, account.getBankId());
            preparedStatement.setDouble(5, account.getBalance());
            preparedStatement.setDate(6, Date.valueOf(account.getLastInterestDate()));
            preparedStatement.setInt(7, accountId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update account", e);
        }
    }

    /**
     * Удалить аккаунт по его идентификатору.
     *
     * @param accountId идентификатор аккаунта, который следует удалить.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public void deleteAccountById(int accountId) {
        String sql = "DELETE FROM account WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, accountId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete account", e);
        }
    }

    /**
     * Преобразовать результат SQL-запроса в объект аккаунта.
     *
     * @param resultSet результат SQL-запроса с данными о аккаунте.
     * @return объект аккаунта.
     * @throws SQLException если произошла ошибка при обработке результата запроса.
     */
    private Account mapAccountFromResultSet(ResultSet resultSet) throws SQLException {
        return new Account(
                resultSet.getInt("id"),
                resultSet.getString("account_number"),
                resultSet.getDate("account_date").toLocalDate(),
                resultSet.getInt("user_id"),
                resultSet.getInt("bank_id"),
                resultSet.getDouble("balance"),
                resultSet.getDate("last_interest_date").toLocalDate()
        );
    }
}
