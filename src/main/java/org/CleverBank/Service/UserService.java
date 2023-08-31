package org.CleverBank.Service;

import org.CleverBank.Models.User;
import org.CleverBank.Repository.UserRepository;

import javax.sql.DataSource;

public class UserService {
    private UserRepository userRepository;


    public UserService(DataSource dataSource) {
        this.userRepository = new UserRepository(dataSource);
    }

    public User getUser(int userId) {
        User user = userRepository.getUserById(userId);
        if (user != null) {
            return user;
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public User saveUser(User user) {

        return userRepository.saveUser(user);
    }

    public void updateUser(User user, int userId) {
        userRepository.updateUserById(user, userId);
    }

    public void deleteUser(int userId) {
        if (userRepository.getUserById(userId) != null) {
            userRepository.deleteUserById(userId);
        } else {
            throw new RuntimeException("User not found");
        }
    }
}
