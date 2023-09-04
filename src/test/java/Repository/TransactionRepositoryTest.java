package Repository;

import org.CleverBank.Models.Transaction;
import org.CleverBank.Models.TransactionType;
import org.CleverBank.Repository.TransactionRepository;
import org.assertj.core.api.Assertions;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
/**
 * Класс TransactionRepositoryTest представляет собой набор юнит-тестов для класса TransactionRepository.
 * Он использует встроенную базу данных H2 для выполнения тестовых операций с базой данных.
 */
public class TransactionRepositoryTest {

    private static TransactionRepository transactionRepository;

    /**
     * Метод setUp выполняется перед запуском всех тестов в классе.
     * Он инициализирует встроенную базу данных H2 и создает объект TransactionRepository для тестирования.
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

        // Создание таблицы "transactions" для хранения данных о транзакциях
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE transactions (id SERIAL PRIMARY KEY, source_account INT, " +
                    "target_account INT, transaction_type VARCHAR(15), amount INT, transaction_date DATE)");
        }

        // Создание объекта TransactionRepository для тестирования
        transactionRepository = new TransactionRepository(dataSource);
    }

    /**
     * Метод testSaveTransaction_findById выполняет тестирование операции сохранения информации о транзакции
     * и поиска транзакции по ее идентификатору.
     */
    @Test
    void testSaveTransaction_findById() {
        // Создание объекта транзакции для тестирования
        Transaction transaction = Transaction.builder()
                .amount(100)
                .sourceAccount(1)
                .date(LocalDate.now())
                .type(TransactionType.DEPOSIT)
                .build();

        // Сохранение информации о транзакции в базе данных
        Transaction savedTransaction = transactionRepository.saveTransaction(transaction);

        // Поиск транзакции по ее идентификатору
        Transaction retrievedTransaction = transactionRepository.getTransactionById(savedTransaction.getId());

        // Проверка, что информация о транзакции была успешно сохранена и найдена
        Assertions.assertThat(savedTransaction.getId()).isNotNull();
        Assertions.assertThat(savedTransaction.getAmount()).isEqualTo(retrievedTransaction.getAmount());
    }

    /**
     * Метод testUpdateTransactionById выполняет тестирование операции обновления информации о транзакции по ее идентификатору.
     */
    @Test
    void testUpdateTransactionById() {
        // Создание объекта транзакции для тестирования
        Transaction transaction = Transaction.builder()
                .amount(100)
                .sourceAccount(1)
                .targetAccount(2)
                .date(LocalDate.EPOCH)
                .type(TransactionType.DEPOSIT)
                .build();

        // Сохранение информации о транзакции в базе данных
        Transaction savedTransaction = transactionRepository.saveTransaction(transaction);

        // Обновление информации о транзакции по ее идентификатору
        transactionRepository.updateTransactionById(Transaction.builder()
                .amount(1000)
                .sourceAccount(1)
                .targetAccount(2)
                .date(LocalDate.EPOCH)
                .type(TransactionType.DEPOSIT)
                .build(), savedTransaction.getId());

        // Получение информации о транзакции после обновления
        Transaction changedTransaction = transactionRepository.getTransactionById(savedTransaction.getId());

        // Проверка, что информация о транзакции была успешно обновлена
        Assertions.assertThat(changedTransaction.getAmount()).isEqualTo(1000);
    }

    /**
     * Метод testDeleteTransaction выполняет тестирование операции удаления транзакции по ее идентификатору.
     */
    @Test
    void testDeleteTransaction() {
        // Создание объекта транзакции для тестирования
        Transaction transaction = Transaction.builder()
                .amount(100)
                .sourceAccount(1)
                .targetAccount(2)
                .date(LocalDate.EPOCH)
                .type(TransactionType.DEPOSIT)
                .build();

        // Сохранение информации о транзакции в базе данных
        transactionRepository.saveTransaction(transaction);

        // Удаление транзакции по ее идентификатору
        transactionRepository.deleteTransactionById(transaction.getId());

        // Попытка получения информации о транзакции после удаления
        Transaction retrievedTransaction = transactionRepository.getTransactionById(transaction.getId());

        // Проверка, что информация о транзакции была успешно удалена
        Assertions.assertThat(retrievedTransaction).isNull();
    }
}
