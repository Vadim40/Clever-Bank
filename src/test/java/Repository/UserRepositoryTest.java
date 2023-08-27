package Repository;

import org.CleverBank.Models.User;
import org.CleverBank.Repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.Test;
import org.h2.jdbcx.JdbcDataSource;


import java.sql.Connection;
import java.sql.Statement;


class UserRepositoryTest {

    private static UserRepository userRepository;

    @BeforeAll
   static void setUp() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            int created = statement.executeUpdate("CREATE TABLE users(\n" +
                    "                        id SERIAL PRIMARY KEY ,\n" +
                    "                        firstname varchar(50),\n" +
                    "                        lastname varchar(50))");
            System.out.println(created);
        }

        userRepository = new UserRepository(dataSource);
    }


    @Test
    void testCreateUser_FindById() {
        User user = User.builder()
                .firstname("dan")
                .lastname("petrov")
                .build();


        User savedUser= userRepository.saveUser(user);
        User retrievedUser=userRepository.getUserById(savedUser.getId());

        Assertions.assertThat(savedUser.getId()).isNotNull();
        Assertions.assertThat(savedUser.getFirstname()).isEqualTo(retrievedUser.getFirstname());
    }

    @Test
    void testDeleteUser(){
        User user = User.builder()
                .id(1)
                .firstname("dan")
                .lastname("petrov")
                .build();
        userRepository.deleteUserById(user.getId());
        User retrievedUser=userRepository.getUserById(user.getId());
        Assertions.assertThat(retrievedUser).isNull();
    }
}
