package Repository;

import org.CleverBank.Models.Account;
import org.CleverBank.Repository.AccountRepository;
import org.assertj.core.api.Assertions;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * Класс AccountRepositoryTest представляет собой набор юнит-тестов для класса AccountRepository.
 * Он использует встроенную базу данных H2 для выполнения тестовых операций с базой данных.
 */
public class AccountRepositoryTest {

    private static AccountRepository accountRepository;

    /**
     * Метод setUp выполняется перед запуском всех тестов в классе.
     * Он инициализирует встроенную базу данных H2 и создает объект AccountRepository для тестирования.
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

        // Создание таблицы "account" для хранения данных об аккаунтах
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE account (id SERIAL PRIMARY  KEY, balance INT, " +
                    "user_id INT, bank_id INT, account_date DATE, account_number VARCHAR(10)," +
                    "last_interest_date DATE)");
        }

        // Создание объекта AccountRepository для тестирования
        accountRepository = new AccountRepository(dataSource);
    }

    /**
     * Метод testSaveAccount_findAccountById выполняет тестирование операции сохранения аккаунта
     * и поиска аккаунта по его идентификатору.
     */
    @Test
    void testSaveAccount_findAccountById() {
        // Создание объекта аккаунта для тестирования
        Account account = Account.builder()
                .accountNumber("1234567890")
                .date(LocalDate.EPOCH)
                .lastInterestDate(LocalDate.EPOCH)
                .balance(1000)
                .bankId(1)
                .userId(1)
                .build();

        // Сохранение аккаунта в базе данных
        Account savedAccount = accountRepository.saveAccount(account);

        // Поиск аккаунта по его идентификатору
        Account retrievedAccount = accountRepository.getAccountById(savedAccount.getId());

        // Проверка, что аккаунт был успешно сохранен и найден
        Assertions.assertThat(savedAccount.getId()).isNotNull();
        Assertions.assertThat(savedAccount.getAccountNumber()).isEqualTo(retrievedAccount.getAccountNumber());
    }

    /**
     * Метод testUpdateAccountById выполняет тестирование операции обновления аккаунта по его идентификатору.
     */
    @Test
    void testUpdateAccountById() {
        // Создание объекта аккаунта для тестирования
        Account account = Account.builder()
                .accountNumber("1234567890")
                .date(LocalDate.EPOCH)
                .lastInterestDate(LocalDate.EPOCH)
                .balance(1000)
                .bankId(1)
                .userId(1)
                .build();

        // Сохранение аккаунта в базе данных
        Account savedAccount = accountRepository.saveAccount(account);

        // Обновление аккаунта по его идентификатору
        accountRepository.updateAccountById(Account.builder()
                .accountNumber("9999999999")
                .date(LocalDate.EPOCH)
                .lastInterestDate(LocalDate.EPOCH)
                .balance(1000)
                .bankId(1)
                .userId(1)
                .build(), savedAccount.getId());

        // Получение аккаунта после обновления
        Account changedAccount = accountRepository.getAccountById(savedAccount.getId());

        // Проверка, что аккаунт был успешно обновлен
        Assertions.assertThat(changedAccount.getAccountNumber()).isEqualTo("9999999999");
    }

    /**
     * Метод testDeleteAccountById выполняет тестирование операции удаления аккаунта по его идентификатору.
     */
    @Test
    void testDeleteAccountById() {
        // Создание объекта аккаунта для тестирования
        Account account = Account.builder()
                .id(1)
                .accountNumber("1234567890")
                .date(LocalDate.EPOCH)
                .lastInterestDate(LocalDate.EPOCH)
                .balance(1000)
                .bankId(1)
                .userId(1)
                .build();

        // Сохранение аккаунта в базе данных
        accountRepository.saveAccount(account);

        // Удаление аккаунта по его идентификатору
        accountRepository.deleteAccountById(account.getId());

        // Попытка получения аккаунта после удаления
        Account retrievedAccount = accountRepository.getAccountById(account.getId());

        // Проверка, что аккаунт был успешно удален
        Assertions.assertThat(retrievedAccount).isNull();
    }
}
