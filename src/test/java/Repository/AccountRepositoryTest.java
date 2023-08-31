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

public class AccountRepositoryTest {
    private static AccountRepository accountRepository;
    @BeforeAll
    static void setUp() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE account (id SERIAl PRIMARY  KEY, balance INT, " +
                    "user_id INT,bank_id INT, account_date DATE, account_number VARCHAR(10)," +
                    "last_interest_date DATE)");
        }
      accountRepository=new AccountRepository(dataSource);

    }
    @Test
    void testSaveAccount_findAccountById(){
        Account account=Account.builder()
                .accountNumber("1234567890")
                .date(LocalDate.EPOCH)
                .lastInterestDate(LocalDate.EPOCH)
                .balance(1000)
                .bankId(1)
                .userId(1)
                .build();
        Account savedAccount=accountRepository.saveAccount(account);
        Account retrievedAccount=accountRepository.getAccountById(savedAccount.getId());

        Assertions.assertThat(savedAccount.getId()).isNotNull();
        Assertions.assertThat(savedAccount.getAccountNumber()).isEqualTo(retrievedAccount.getAccountNumber());
    }
    @Test
    void testUpdateAccountById() {
        Account account=Account.builder()
                .accountNumber("1234567890")
                .date(LocalDate.EPOCH)
                .lastInterestDate(LocalDate.EPOCH)
                .balance(1000)
                .bankId(1)
                .userId(1)
                .build();
        Account savedAccount=accountRepository.saveAccount(account);
        accountRepository.updateAccountById(Account.builder()
                .accountNumber("9999999999")
                .date(LocalDate.EPOCH)
                .lastInterestDate(LocalDate.EPOCH)
                .balance(1000)
                .bankId(1)
                .userId(1)
                .build(),savedAccount.getId());
        Account changedAccount=accountRepository.getAccountById(savedAccount.getId());

        Assertions.assertThat(changedAccount.getAccountNumber()).isEqualTo("9999999999");
    }
    @Test
    void testDeleteAccountById(){
        Account account=Account.builder()
                .id(1)
                .accountNumber("1234567890")
                .date(LocalDate.EPOCH)
                .lastInterestDate(LocalDate.EPOCH)
                .balance(1000)
                .bankId(1)
                .userId(1)
                .build();
        accountRepository.saveAccount(account);
        accountRepository.deleteAccountById(account.getId());
        Account retrievedAccount=accountRepository.getAccountById(account.getId());

        Assertions.assertThat(retrievedAccount).isNull();
    }
}
