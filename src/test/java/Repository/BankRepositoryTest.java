package Repository;

import org.CleverBank.Models.Bank;
import org.CleverBank.Repository.BankRepository;
import org.assertj.core.api.Assertions;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;

public class BankRepositoryTest {
    private static BankRepository bankRepository;
    @BeforeAll
    static void setUp() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE bank (id SERIAl PRIMARY KEY, name VARCHAR(50))");
        }
        bankRepository=new BankRepository(dataSource);

    }
    @Test
    void testUpdateBankById(){
        Bank bank=Bank.builder()
                .name("Tinkoff")
                .build();


       Bank savedBank= bankRepository.saveBank(bank);
        bankRepository.updateBankById(Bank.builder().name("BSB").build(),savedBank.getId());
        Bank changedBank=bankRepository.getBankById(savedBank.getId());

        Assertions.assertThat(changedBank.getName()).isEqualTo("BSB");
    }
    @Test
    void testSaveBank_FindById(){
        Bank bank=Bank.builder()
                .name("Tinkoff")
                .build();
        Bank savedBank=bankRepository.saveBank(bank);
        Bank retrieveBank=bankRepository.getBankById(savedBank.getId());

        Assertions.assertThat(savedBank.getId()).isNotNull();
        Assertions.assertThat(savedBank.getName()).isEqualTo(retrieveBank.getName());
    }


    @Test
    void testDeleteBankById(){
        Bank bank=Bank.builder()
                .id(2)
                .name("Tinkoff")
                .build();
        bankRepository.saveBank(bank);
        bankRepository.deleteBankById(bank.getId());
        Bank retrieveBank=bankRepository.getBankById(bank.getId());

        Assertions.assertThat(retrieveBank).isNull();
    }

}
