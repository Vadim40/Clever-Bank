package Services;

import org.CleverBank.Models.Account;
import org.CleverBank.Repository.AccountRepository;
import org.CleverBank.Service.AccountService;
import org.CleverBank.Service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Класс AccountServiceTest представляет собой набор юнит-тестов для класса AccountService.
 * Здесь тестируются методы, связанные с операциями на аккаунтах.
 */
public class AccountServiceTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private ScheduledExecutorService scheduler;

    @Mock
    private ExecutorService interestExecutor;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionService transactionService;

    private AccountService accountService;

    /**
     * Метод настройки перед каждым тестом. Здесь создаются заглушки (mocks) для зависимых классов
     * и настраивается объект AccountService для тестирования.
     */
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountService(dataSource);
        scheduler = Executors.newScheduledThreadPool(1);
        interestExecutor = Executors.newFixedThreadPool(10);
        accountService.setScheduler(scheduler);
        accountService.setInterestExecutor(interestExecutor);
        accountService.setAccountRepository(accountRepository);
        accountService.setTransactionService(transactionService);
    }

    /**
     * Тест метода deposit, который проверяет корректное пополнение средств на аккаунте.
     * Проверяется вызов метода updateAccountById у AccountRepository.
     */
    @Test
    public void testDeposit() {
        when(accountRepository.getAccountById(anyInt())).thenReturn(createAccount(1));
        accountService.setAccountRepository(accountRepository);
        Account account = createAccount(1);
        double amount = 500.0;
        accountService.deposit(account, amount);
        verify(accountRepository).updateAccountById(account, account.getId());
    }

    /**
     * Тест метода withdraw, который проверяет корректное снятие средств с аккаунта.
     * Проверяется вызов метода updateAccountById у AccountRepository.
     */
    @Test
    public void testWithdraw() {
        when(accountRepository.getAccountById(anyInt())).thenReturn(createAccount(1));
        accountService.setAccountRepository(accountRepository);
        Account account = createAccount(1);
        double amount = 200.0;
        accountService.withdraw(account, amount);
        verify(accountRepository).updateAccountById(account, account.getId());
    }

    /**
     * Тест метода transfer, который проверяет корректный перевод средств между аккаунтами.
     * Проверяется вызов метода updateAccountById у AccountRepository и commit у Connection.
     * @throws SQLException если возникает ошибка SQL во время теста.
     */
    @Test
    public void testTransfer() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(accountRepository.getAccountById(anyInt())).thenReturn(createAccount(1));
        accountService.setAccountRepository(accountRepository);
        accountService.setDataSource(dataSource);
        Account sourceAccount = createAccount(1);
        Account targetAccount = createAccount(2);
        double amount = 100.0;
        accountService.transfer(sourceAccount, targetAccount, amount);
        verify(accountRepository, times(2)).updateAccountById(any(Account.class), anyInt());
        verify(connection).commit();
    }

    /**
     * Тест метода getAccount, который проверяет корректное получение аккаунта по его ID.
     */
    @Test
    public void testGetAccount() {
        accountRepository = mock(AccountRepository.class);
        accountService.setAccountRepository(accountRepository);
        int accountId = 1;
        Account expectedAccount = createAccount(accountId);
        when(accountRepository.getAccountById(accountId)).thenReturn(expectedAccount);
        Account retrievedAccount = accountService.getAccount(accountId);
        assertThat(retrievedAccount)
                .isNotNull()
                .isEqualTo(expectedAccount);
    }

    /**
     * Тест метода getAllAccounts, который проверяет корректное получение списка всех аккаунтов.
     */
    @Test
    public void testGetAllAccounts() {
        accountRepository = mock(AccountRepository.class);
        accountService.setAccountRepository(accountRepository);
        List<Account> expectedAccounts = new ArrayList<>();
        expectedAccounts.add(createAccount(1));
        expectedAccounts.add(createAccount(2));
        when(accountRepository.getAllAccounts()).thenReturn(expectedAccounts);
        List<Account> accounts = accountService.getAllAccounts();
        assertThat(accounts)
                .isNotNull()
                .hasSize(2)
                .containsExactlyElementsOf(expectedAccounts);
    }

    /**
     * Тест метода saveAccount, который проверяет корректное сохранение аккаунта в репозитории.
     */
    @Test
    public void testSaveAccount() {
        accountRepository = mock(AccountRepository.class);
        accountService.setAccountRepository(accountRepository);
        Account accountToSave = createAccount(1);
        when(accountRepository.saveAccount(accountToSave)).thenReturn(accountToSave);
        Account savedAccount = accountService.saveAccount(accountToSave);
        assertThat(savedAccount)
                .isNotNull()
                .isEqualTo(accountToSave);
    }

    /**
     * Тест метода updateAccount, который проверяет корректное обновление аккаунта в репозитории.
     */
    @Test
    public void testUpdateAccount() {
        accountRepository = mock(AccountRepository.class);
        accountService.setAccountRepository(accountRepository);
        int accountId = 1;
        Account updatedAccount = createAccount(accountId);
        updatedAccount.setBalance(2000.0);
        when(accountRepository.getAccountById(accountId)).thenReturn(updatedAccount);
        accountService.updateAccount(updatedAccount, accountId);
        verify(accountRepository).updateAccountById(updatedAccount, accountId);
    }

    /**
     * Тест метода deleteAccount, который проверяет корректное удаление аккаунта из репозитория.
     */
    @Test
    public void testDeleteAccount() {
        accountRepository = mock(AccountRepository.class);
        accountService.setAccountRepository(accountRepository);
        int accountId = 1;
        Account accountToDelete = createAccount(accountId);
        when(accountRepository.getAccountById(accountId)).thenReturn(accountToDelete);
        accountService.deleteAccount(accountId);
        verify(accountRepository).deleteAccountById(accountId);
    }

    /**
     * Вспомогательный метод для создания объекта Account с заданным ID.
     *
     * @param id ID аккаунта.
     * @return Объект Account с указанным ID.
     */
    private Account createAccount(int id) {
        return Account.builder()
                .id(id)
                .accountNumber("123456789" + id)
                .date(LocalDate.now())
                .userId(id)
                .bankId(id)
                .balance(1000.0)
                .lastInterestDate(LocalDate.now())
                .build();
    }
}
