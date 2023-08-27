package org.CleverBank.Repository;

import org.CleverBank.Models.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

   private DataSource dataSource;

    public UserRepository(DataSource dataSource){
        this.dataSource=dataSource;
    }

    public UserRepository() throws SQLException {
    }

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


    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add((mapUserFromResultSet(resultSet)));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get users", e);
        }
    }

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
                    user.setId(generatedId); // Устанавливаем сгенерированный id в объект User
                } else {
                    throw new RuntimeException("Failed to get generated user id");
                }
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }
    public User updateUserById(User user, int userId) {
        String sql = "UPDATE users SET firstname=?, lastname=? WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getFirstname());
            preparedStatement.setString(2, user.getLastname());
            preparedStatement.setInt(3, userId);
            preparedStatement.executeUpdate();
            user.setId(userId);
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

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

    private User mapUserFromResultSet(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("id"),
                resultSet.getString("firstname"),
                resultSet.getString("lastname")
        );
    }
}
