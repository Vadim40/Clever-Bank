package Repository;

import org.CleverBank.Models.User;
import org.CleverBank.Repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.Test;
import org.h2.jdbcx.JdbcDataSource;


import java.sql.Connection;
import java.sql.Statement;

/**
 * Класс UserRepositoryTest представляет собой набор юнит-тестов для класса UserRepository.
 * Он использует встроенную базу данных H2 для выполнения тестовых операций с базой данных.
 */
class UserRepositoryTest {

    private static UserRepository userRepository;

    /**
     * Метод setUp выполняется перед запуском всех тестов в классе.
     * Он инициализирует встроенную базу данных H2 и создает объект UserRepository для тестирования.
     *
     * @throws Exception Если возникают ошибки при настройке тестового окружения.
     */
    @BeforeAll
    static void setUp() throws Exception {
        // Инициализация встроенной базы данных H2
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        // Создание таблицы "users" для хранения данных о пользователях
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            int created = statement.executeUpdate("CREATE TABLE users(id SERIAL PRIMARY KEY, " +
                    "firstname varchar(50), lastname varchar(50))");
        }

        // Создание объекта UserRepository для тестирования
        userRepository = new UserRepository(dataSource);
    }

    /**
     * Метод testCreateUser_FindById выполняет тестирование операции создания пользователя и поиска пользователя по его идентификатору.
     */
    @Test
    void testCreateUser_FindById() {
        // Создание объекта пользователя для тестирования
        User user = User.builder()
                .firstname("dan")
                .lastname("petrov")
                .build();

        // Создание пользователя в базе данных
        User savedUser = userRepository.saveUser(user);

        // Поиск пользователя по его идентификатору
        User retrievedUser = userRepository.getUserById(savedUser.getId());

        // Проверка, что пользователь был успешно создан и найден
        Assertions.assertThat(savedUser.getId()).isNotNull();
        Assertions.assertThat(savedUser.getFirstname()).isEqualTo(retrievedUser.getFirstname());
    }

    /**
     * Метод testUpdateUser выполняет тестирование операции обновления информации о пользователе.
     */
    @Test
    void testUpdateUser() {
        // Создание объекта пользователя для тестирования
        User user = User.builder()
                .firstname("dan")
                .lastname("petrov")
                .build();

        // Создание пользователя в базе данных
        User savedUser = userRepository.saveUser(user);

        // Обновление информации о пользователе
        userRepository.updateUserById(User.builder().firstname("Ivan").lastname("Petrov").build(), savedUser.getId());

        // Получение информации о пользователе после обновления
        User changedUser = userRepository.getUserById(savedUser.getId());

        // Проверка, что информация о пользователе была успешно обновлена
        Assertions.assertThat(changedUser.getFirstname()).isEqualTo("Ivan");
    }

    /**
     * Метод testDeleteUser выполняет тестирование операции удаления пользователя по его идентификатору.
     */
    @Test
    void testDeleteUser() {
        // Создание объекта пользователя для тестирования
        User user = User.builder()
                .id(1)
                .firstname("dan")
                .lastname("petrov")
                .build();

        // Удаление пользователя по его идентификатору
        userRepository.deleteUserById(user.getId());

        // Попытка получения информации о пользователе после удаления
        User retrievedUser = userRepository.getUserById(user.getId());

        // Проверка, что информация о пользователе была успешно удалена
        Assertions.assertThat(retrievedUser).isNull();
    }
}
