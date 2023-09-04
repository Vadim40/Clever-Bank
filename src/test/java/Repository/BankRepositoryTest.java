package Repository;

import org.CleverBank.Models.Bank;
import org.CleverBank.Repository.BankRepository;
import org.assertj.core.api.Assertions;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
/**
 * Класс BankRepositoryTest представляет собой набор юнит-тестов для класса BankRepository.
 * Он использует встроенную базу данных H2 для выполнения тестовых операций с базой данных.
 */
public class BankRepositoryTest {

    private static BankRepository bankRepository;

    /**
     * Метод setUp выполняется перед запуском всех тестов в классе.
     * Он инициализирует встроенную базу данных H2 и создает объект BankRepository для тестирования.
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

        // Создание таблицы "bank" для хранения данных о банках
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE bank (id SERIAL PRIMARY KEY, name VARCHAR(50))");
        }

        // Создание объекта BankRepository для тестирования
        bankRepository = new BankRepository(dataSource);
    }

    /**
     * Метод testUpdateBankById выполняет тестирование операции обновления информации о банке по его идентификатору.
     */
    @Test
    void testUpdateBankById() {
        // Создание объекта банка для тестирования
        Bank bank = Bank.builder()
                .name("Tinkoff")
                .build();

        // Сохранение информации о банке в базе данных
        Bank savedBank = bankRepository.saveBank(bank);

        // Обновление информации о банке по его идентификатору
        bankRepository.updateBankById(Bank.builder().name("BSB").build(), savedBank.getId());

        // Получение информации о банке после обновления
        Bank changedBank = bankRepository.getBankById(savedBank.getId());

        // Проверка, что информация о банке была успешно обновлена
        Assertions.assertThat(changedBank.getName()).isEqualTo("BSB");
    }

    /**
     * Метод testSaveBank_FindById выполняет тестирование операции сохранения информации о банке
     * и поиска банка по его идентификатору.
     */
    @Test
    void testSaveBank_FindById() {
        // Создание объекта банка для тестирования
        Bank bank = Bank.builder()
                .name("Tinkoff")
                .build();

        // Сохранение информации о банке в базе данных
        Bank savedBank = bankRepository.saveBank(bank);

        // Поиск банка по его идентификатору
        Bank retrievedBank = bankRepository.getBankById(savedBank.getId());

        // Проверка, что информация о банке была успешно сохранена и найдена
        Assertions.assertThat(savedBank.getId()).isNotNull();
        Assertions.assertThat(savedBank.getName()).isEqualTo(retrievedBank.getName());
    }

    /**
     * Метод testDeleteBankById выполняет тестирование операции удаления банка по его идентификатору.
     */
    @Test
    void testDeleteBankById() {
        // Создание объекта банка для тестирования
        Bank bank = Bank.builder()
                .id(2)
                .name("Tinkoff")
                .build();

        // Сохранение информации о банке в базе данных
        bankRepository.saveBank(bank);

        // Удаление банка по его идентификатору
        bankRepository.deleteBankById(bank.getId());

        // Попытка получения информации о банке после удаления
        Bank retrievedBank = bankRepository.getBankById(bank.getId());

        // Проверка, что информация о банке была успешно удалена
        Assertions.assertThat(retrievedBank).isNull();
    }
}
