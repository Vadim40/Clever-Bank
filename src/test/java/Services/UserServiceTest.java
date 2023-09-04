package Services;

import org.CleverBank.Models.Account;
import org.CleverBank.Models.Transaction;
import org.CleverBank.Models.TransactionType;
import org.CleverBank.Models.User;
import org.CleverBank.Repository.AccountRepository;
import org.CleverBank.Repository.TransactionRepository;
import org.CleverBank.Repository.UserRepository;
import org.CleverBank.Service.DocumentGenerator;
import org.CleverBank.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    @Mock
    private DataSource dataSource;

    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(dataSource);
    }

    /**
     * Тест метода generateStatementForUser, который проверяет корректную генерацию выписки о транзакциях пользователя.
     */
    @Test
    public void testGenerateStatementForUser() {
        UserRepository userRepository = mock(UserRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        AccountRepository accountRepository = mock(AccountRepository.class);
        DocumentGenerator documentGenerator = mock(DocumentGenerator.class);
        when(documentGenerator.generateTransactionStatement(any(User.class), any(Account.class), anyList()))
                .thenReturn(new StringBuilder("Expected Statement Contents"));

        when(userRepository.getUserById(1)).thenReturn(createTestUser(1));
        when(accountRepository.getAccountById(1)).thenReturn(createTestAccount(1));

        userService.setUserRepository(userRepository);
        userService.setTransactionRepository(transactionRepository);
        userService.setAccountRepository(accountRepository);
        userService.setDocumentGenerator(documentGenerator);

        User user = createTestUser(1);
        Account account = createTestAccount(1);
        List<Transaction> transactions = createTestTransactions(user, account, 3);

        when(transactionRepository.getAllTransactionsByAccountIdAndTime(1, LocalDate.now().minusDays(7), LocalDate.now()))
                .thenReturn(transactions);

        StringBuilder statement = userService.generateStatementForUser(1, 1, LocalDate.now().minusDays(7), LocalDate.now());

        assertThat(statement).isNotNull();
    }


    /**
     * Тест метода getUser, который проверяет корректное получение пользователя по ID.
     */
    @Test
    public void testGetUser() {
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.getUserById(1)).thenReturn(createTestUser(1));

        userService.setUserRepository(userRepository);

        User user = userService.getUser(1);

        assertThat(user).isEqualTo(createTestUser(1));
    }

    /**
     * Тест метода getAllUsers, который проверяет корректное получение списка всех пользователей.
     */
    @Test
    public void testGetAllUsers() {
        UserRepository userRepository = mock(UserRepository.class);
        List<User> expectedUsers = List.of(
                createTestUser(1),
                createTestUser(2)
        );
        when(userRepository.getAllUsers()).thenReturn(expectedUsers);

        userService.setUserRepository(userRepository);

        List<User> users = userService.getAllUsers();

        assertThat(users).isNotNull();
        assertThat(users).hasSize(2);
    }

    /**
     * Тест метода saveUser, который проверяет корректное сохранение пользователя в репозитории.
     */
    @Test
    public void testSaveUser() {
        UserRepository userRepository = mock(UserRepository.class);
        User newUser = createTestUser(1);
        when(userRepository.saveUser(newUser)).thenReturn(newUser);

        userService.setUserRepository(userRepository);

        User savedUser = userService.saveUser(newUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser).isEqualTo(newUser);
    }

    /**
     * Тест метода updateUser, который проверяет корректное обновление пользователя в репозитории.
     */
    @Test
    public void testUpdateUser() {
        UserRepository userRepository = mock(UserRepository.class);
        User updatedUser = createTestUser(1);
        doNothing().when(userRepository).updateUserById(updatedUser, 1);

        userService.setUserRepository(userRepository);

        assertThatCode(() -> userService.updateUser(updatedUser, 1)).doesNotThrowAnyException();
    }

    /**
     * Тест метода deleteUser, который проверяет корректное удаление пользователя из репозитория.
     */
    @Test
    public void testDeleteUser() {
        UserRepository userRepository = mock(UserRepository.class);
        User userToDelete = createTestUser(1);
        when(userRepository.getUserById(1)).thenReturn(userToDelete);
        doNothing().when(userRepository).deleteUserById(1);

        userService.setUserRepository(userRepository);

        assertThatCode(() -> userService.deleteUser(1)).doesNotThrowAnyException();
    }

    // Дополнительные вспомогательные методы для создания тестовых объектов

    private User createTestUser(int id) {
        return User.builder()
                .id(id)
                .firstname("Jane")
                .lastname("Petrov")
                .build();
    }

    private Account createTestAccount(int id) {
        return Account.builder()
                .id(id)
                .accountNumber("1234567890" + id)
                .date(LocalDate.now())
                .userId(1)
                .bankId(1)
                .balance(1000.0)
                .lastInterestDate(LocalDate.now())
                .build();
    }

    private List<Transaction> createTestTransactions(User user, Account account, int count) {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Transaction transaction = Transaction.builder()
                    .id(i)
                    .amount(100.0)
                    .sourceAccount(account.getId())
                    .targetAccount(account.getId())
                    .date(LocalDate.now().minusDays(i))
                    .type(TransactionType.DEPOSIT)
                    .build();
            transactions.add(transaction);
        }
        return transactions;
    }
}
