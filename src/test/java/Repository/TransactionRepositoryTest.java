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

public class TransactionRepositoryTest {


    private static TransactionRepository transactionRepository;

    @BeforeAll
   static void setUp() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE transactions (id SERIAl PRIMARY  KEY, source_account INT," +
                    " target_account INT,transaction_type VARCHAR(15), amount INT, transaction_date DATE)");
        }
     transactionRepository= new TransactionRepository(dataSource);

    }

    @Test
    void testSaveTransaction_findById(){
        Transaction transaction= Transaction.builder()
                .amount(100)
                .sourceAccount(1)
                .targetAccount(2)
                .date(LocalDate.EPOCH)
                .type(TransactionType.DEPOSIT)
                .build();
        Transaction savedTransaction= transactionRepository.saveTransaction(transaction);
        Transaction retrievedTransaction=transactionRepository.getTransactionById(savedTransaction.getId());

        Assertions.assertThat(savedTransaction.getId()).isNotNull();
       Assertions.assertThat(savedTransaction.getAmount()).isEqualTo(retrievedTransaction.getAmount());
    }

    @Test
    void testUpdateTransactionById(){
        Transaction transaction= Transaction.builder()
                .amount(100)
                .sourceAccount(1)
                .targetAccount(2)
                .date(LocalDate.EPOCH)
                .type(TransactionType.DEPOSIT)
                .build();

        Transaction savedTransaction= transactionRepository.saveTransaction(transaction);
            transactionRepository.updateTransactionById(Transaction.builder()
                    .amount(1000).sourceAccount(1).targetAccount(2).date(LocalDate.EPOCH).type(TransactionType.DEPOSIT).build(),savedTransaction.getId());
            Transaction changedTransaction=transactionRepository.getTransactionById(savedTransaction.getId());

            Assertions.assertThat(changedTransaction.getAmount()).isEqualTo(1000);
    }
    @Test
    void testDeleteTransaction(){
        Transaction transaction= Transaction.builder()
                .amount(100)
                .sourceAccount(1)
                .targetAccount(2)
                .date(LocalDate.EPOCH)
                .type(TransactionType.DEPOSIT)
                .build();
       transactionRepository.saveTransaction(transaction);
       transactionRepository.deleteTransactionById(transaction.getId());
        Transaction retrievedTransaction=transactionRepository.getTransactionById(transaction.getId());

        Assertions.assertThat(retrievedTransaction).isNull();
    }

}
