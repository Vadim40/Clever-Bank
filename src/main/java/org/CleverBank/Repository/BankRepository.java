/**
 * Репозиторий для работы с банками.
 */
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

    /**
     * Конструктор класса BankRepository.
     *
     * @param dataSource источник данных для выполнения операций с базой данных.
     */
    public BankRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Получить банк по его идентификатору.
     *
     * @param bankId идентификатор банка.
     * @return объект банка, если найден, в противном случае null.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
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
            throw new RuntimeException("Failed to get bank by ID", e);
        }
    }

    /**
     * Получить список всех банков.
     *
     * @return список объектов банков.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public List<Bank> getAllBanks() {
        String sql = "SELECT * FROM bank";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Bank> banks = new ArrayList<>();
                while (resultSet.next()) {
                    banks.add(mapBankFromResultSet(resultSet));
                }
                return banks;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all banks", e);
        }
    }

    /**
     * Сохранить банк в базе данных.
     *
     * @param bank объект банка для сохранения.
     * @return объект банка с установленным идентификатором.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public Bank saveBank(Bank bank) {
        String sql = "INSERT INTO bank (name) VALUES (?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, bank.getName());
            preparedStatement.executeUpdate();
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    bank.setId(generatedId);
                } else {
                    throw new RuntimeException("Failed to get generated bank ID");
                }
            }
            return bank;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create bank", e);
        }
    }

    /**
     * Обновить банк по его идентификатору.
     *
     * @param bank    объект банка с обновленными данными.
     * @param bankId  идентификатор банка, который следует обновить.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public void updateBankById(Bank bank, int bankId) {
        String sql = "UPDATE bank SET name=? WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, bank.getName());
            preparedStatement.setInt(2, bankId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update bank", e);
        }
    }

    /**
     * Удалить банк по его идентификатору.
     *
     * @param bankId идентификатор банка, который следует удалить.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public void deleteBankById(int bankId) {
        String sql = "DELETE FROM bank WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, bankId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete bank", e);
        }
    }

    /**
     * Преобразовать результат SQL-запроса в объект банка.
     *
     * @param resultSet результат SQL-запроса с данными о банке.
     * @return объект банка.
     * @throws SQLException если произошла ошибка при обработке результата запроса.
     */
    private Bank mapBankFromResultSet(ResultSet resultSet) throws SQLException {
        return new Bank(
                resultSet.getInt("id"),
                resultSet.getString("name")
        );
    }
}
