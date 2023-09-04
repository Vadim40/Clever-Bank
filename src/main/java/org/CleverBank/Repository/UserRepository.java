/**
 * Репозиторий для работы с пользователями.
 */
package org.CleverBank.Repository;

import org.CleverBank.Models.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private DataSource dataSource;

    /**
     * Конструктор класса UserRepository.
     *
     * @param dataSource источник данных для выполнения операций с базой данных.
     */
    public UserRepository(DataSource dataSource){
        this.dataSource=dataSource;
    }

    /**
     * Получить пользователя по его идентификатору.
     *
     * @param userId идентификатор пользователя.
     * @return объект пользователя, если найден, в противном случае null.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUserFromResultSet(resultSet);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get user", e);
        }
    }

    /**
     * Получить список всех пользователей.
     *
     * @return список объектов пользователей.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(mapUserFromResultSet(resultSet));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get users", e);
        }
    }

    /**
     * Сохранить пользователя в базе данных.
     *
     * @param user объект пользователя для сохранения.
     * @return объект пользователя с установленным идентификатором.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public User saveUser(User user) {
        String sql = "INSERT INTO users (firstname, lastname) VALUES (?,?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, user.getFirstname());
            preparedStatement.setString(2, user.getLastname());
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    user.setId(generatedId);
                } else {
                    throw new RuntimeException("Failed to get generated user id");
                }
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    /**
     * Обновить пользователя по его идентификатору.
     *
     * @param user   объект пользователя с обновленными данными.
     * @param userId идентификатор пользователя, которого следует обновить.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public void updateUserById(User user, int userId) {
        String sql = "UPDATE users SET firstname=?, lastname=? WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getFirstname());
            preparedStatement.setString(2, user.getLastname());
            preparedStatement.setInt(3, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    /**
     * Удалить пользователя по его идентификатору.
     *
     * @param userId идентификатор пользователя, которого следует удалить.
     * @throws RuntimeException если произошла ошибка при выполнении запроса.
     */
    public void deleteUserById(int userId) {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user ", e);
        }
    }

    /**
     * Преобразовать результат SQL-запроса в объект пользователя.
     *
     * @param resultSet результат SQL-запроса с данными пользователя.
     * @return объект пользователя.
     * @throws SQLException если произошла ошибка при обработке результата запроса.
     */
    private User mapUserFromResultSet(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("id"),
                resultSet.getString("firstname"),
                resultSet.getString("lastname")
        );
    }
}
