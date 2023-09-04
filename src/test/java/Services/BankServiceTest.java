package Services;

import org.CleverBank.Models.Bank;
import org.CleverBank.Repository.BankRepository;
import org.CleverBank.Service.BankService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class BankServiceTest {
    @Mock
    private DataSource dataSource;

    private BankService bankService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        bankService = new BankService(dataSource);
    }

    /**
     * Тест метода getBank, который проверяет корректное получение банка по ID.
     */
    @Test
    public void testGetBankById() {
        BankRepository bankRepository = mock(BankRepository.class);
        Bank expectedBank = Bank.builder()
                .id(1)
                .name("Clever Bank")
                .build();
        when(bankRepository.getBankById(1)).thenReturn(expectedBank);

        bankService.setBankRepository(bankRepository);

        Bank bank = bankService.getBank(1);
        assertThat(bank).isNotNull();
        assertThat(bank.getName()).isEqualTo("Clever Bank");
    }

    /**
     * Тест метода getAllBanks, который проверяет корректное получение списка всех банков.
     */
    @Test
    public void testGetAllBanks() {
        BankRepository bankRepository = mock(BankRepository.class);
        List<Bank> expectedBanks = List.of(
                Bank.builder().id(1).name("Bank1").build(),
                Bank.builder().id(2).name("Bank2").build()
        );
        when(bankRepository.getAllBanks()).thenReturn(expectedBanks);

        bankService.setBankRepository(bankRepository);

        List<Bank> banks = bankService.getAllBanks();
        assertThat(banks.size()).isEqualTo(2);
        assertThat(banks.get(0).getName()).isEqualTo("Bank1");
        assertThat(banks.get(1).getName()).isEqualTo("Bank2");
    }

    /**
     * Тест метода saveBank, который проверяет корректное сохранение банка в репозитории.
     */
    @Test
    public void testSaveBank() {
        BankRepository bankRepository = mock(BankRepository.class);
        Bank bankToSave = Bank.builder()
                .id(3)
                .name("Bank3")
                .build();

        when(bankRepository.saveBank(bankToSave)).thenReturn(bankToSave);

        bankService.setBankRepository(bankRepository);

        Bank savedBank = bankService.saveBank(bankToSave);
        assertThat(savedBank).isEqualTo(bankToSave);
    }

    /**
     * Тест метода updateBank, который проверяет корректное обновление банка в репозитории.
     */
    @Test
    public void testUpdateBank() {
        BankRepository bankRepository = mock(BankRepository.class);
        Bank updatedBank = Bank.builder()
                .id(1)
                .name("UpdatedBank")
                .build();

        bankService.setBankRepository(bankRepository);

        bankService.updateBank(updatedBank, 1);
        verify(bankRepository, times(1)).updateBankById(updatedBank, 1);
    }

    /**
     * Тест метода deleteBank, который проверяет корректное удаление банка из репозитория.
     */
    @Test
    public void testDeleteBank() {
        BankRepository bankRepository = mock(BankRepository.class);
        Bank bankToDelete = Bank.builder()
                .id(1)
                .name("BankToDelete")
                .build();
        when(bankRepository.getBankById(1)).thenReturn(bankToDelete);

        bankService.setBankRepository(bankRepository);

        bankService.deleteBank(1);
        verify(bankRepository, times(1)).deleteBankById(1);
    }
}
