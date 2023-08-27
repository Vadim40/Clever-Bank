package org.CleverBank.Repository;



import org.CleverBank.Models.Account;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountRepository {

    private  DataSource dataSource;
    public AccountRepository(DataSource dataSource) {
        this.dataSource=dataSource;
    }

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
            throw new RuntimeException("Failed to get account", e);
        }
    }


    public List<Account> getAllAccounts() {
        String sql = "SELECT * FROM account";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Account> accounts = new ArrayList<>();
                while (resultSet.next()) {
                    accounts.add((mapAccountFromResultSet(resultSet)));
                }
                return accounts;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get accounts", e);
        }
    }

    public Account saveAccount(Account account) {
        String sql = "INSERT INTO account (account_number, account_date,user_id,bank_id,balance) VALUES (?,?,?,?,?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, account.getAccountNumber());
            preparedStatement.setDate(2, Date.valueOf(account.getDate()));
            preparedStatement.setInt(3, account.getUserId());
            preparedStatement.setInt(4, account.getBankId());
            preparedStatement.setInt(5, account.getBalance());
            preparedStatement.executeUpdate();
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    account.setId(generatedId); // Устанавливаем сгенерированный id в объект User
                } else {
                    throw new RuntimeException("Failed to get generated account id");
                }
            }
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to crete account", e);
        }
    }

    public Account updateAccountById(Account account, int accountId) {
        String sql = "UPDATE account SET account_number = ?, account_date=?, user_id = ?, bank_id = ?, balance = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, account.getAccountNumber());
            preparedStatement.setDate(2, Date.valueOf(account.getDate()));
            preparedStatement.setInt(3, account.getUserId());
            preparedStatement.setInt(4, account.getBankId());
            preparedStatement.setInt(5, account.getBalance());
            preparedStatement.setInt(6, accountId);
            preparedStatement.executeUpdate();
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("failed to update account", e);
        }
    }

    public void deleteAccountById(int accountId) {
        String sql = "DELETE FROM account WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, accountId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("failed to delete account", e);
        }
    }

    private Account mapAccountFromResultSet(ResultSet resultSet) throws SQLException {
        return new Account(
                resultSet.getInt("id"),
                resultSet.getString("account_number"),
                resultSet.getDate("account_date").toLocalDate(),
                resultSet.getInt("user_id"),
                resultSet.getInt("bank_id"),
                resultSet.getInt("balance")
        );
    }
}
