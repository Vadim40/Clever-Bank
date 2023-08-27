package org.CleverBank.Repository;

import org.CleverBank.Models.Bank;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BankRepository {

    private DataSource dataSource;

    public BankRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Bank getBankById(int bankId) {
        String sql = "SELECT * FROM bank WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, bankId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapBankFromResultSet(resultSet);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public List<Bank> getAllBanks() {
        String sql = "SELECT * FROM bank";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Bank> banks = new ArrayList<>();
                while (resultSet.next()) {
                    banks.add((mapBankFromResultSet(resultSet)));
                }
                return banks;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get banks", e);
        }
    }

    public Bank saveBank(Bank bank) {
        String sql = "INSERT INTO bank (name) VALUES (?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, bank.getName());
            preparedStatement.executeUpdate();
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    bank.setId(generatedId); // Устанавливаем сгенерированный id в объект User
                } else {
                    throw new RuntimeException("Failed to get generated bank id");
                }
            }
            return bank;
        } catch (SQLException e) {
            throw new RuntimeException("failed to create bank", e);
        }
    }

    public Bank updateBankById(Bank bank, int bankId) {
        String sql = "UPDATE bank SET name=?,  WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, bank.getName());
            preparedStatement.setInt(2, bankId);
            preparedStatement.executeUpdate();
            return bank;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update bank", e);
        }
    }

    public void deleteBankById(int bankId) {
        String sql = "DELETE  FROM bank WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, bankId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete bank", e);
        }
    }

    private Bank mapBankFromResultSet(ResultSet resultSet) throws SQLException {
        return new Bank(
                resultSet.getInt("id"),
                resultSet.getString("name")
        );
    }
}
