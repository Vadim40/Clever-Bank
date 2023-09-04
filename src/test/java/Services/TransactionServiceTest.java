package Services;

import org.CleverBank.Models.Account;
import org.CleverBank.Models.Bank;
import org.CleverBank.Models.Transaction;
import org.CleverBank.Models.TransactionType;
import org.CleverBank.Repository.BankRepository;
import org.CleverBank.Repository.TransactionRepository;
import org.CleverBank.Service.DocumentGenerator;
import org.CleverBank.Service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TransactionServiceTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private BankRepository bankRepository;

    @Mock
    private DocumentGenerator documentGenerator;

    private TransactionService transactionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionService = new TransactionService(dataSource);
        transactionService.setBankRepository(bankRepository);
        transactionService.setDocumentGenerator(documentGenerator);
    }

    /**
     * Тест метода getTransaction, который проверяет корректное получение транзакции по ID.
     */
    @Test
    public void testGetTransaction() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        transactionService.setTransactionRepository(transactionRepository);

        int transactionId = 1;
        Transaction expectedTransaction = createTransaction(transactionId);

        when(transactionRepository.getTransactionById(transactionId)).thenReturn(expectedTransaction);

        Transaction retrievedTransaction = transactionService.getTransaction(transactionId);

        assertThat(retrievedTransaction)
                .isNotNull()
                .isEqualTo(expectedTransaction);
    }

    /**
     * Тест метода getAllTransaction, который проверяет корректное получение списка всех транзакций.
     */
    @Test
    public void testGetAllTransaction() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        transactionService.setTransactionRepository(transactionRepository);

        List<Transaction> expectedTransactions = List.of(
                createTransaction(1),
                createTransaction(2)
        );

        when(transactionRepository.getAllTransactions()).thenReturn(expectedTransactions);

        List<Transaction> transactions = transactionService.getAllTransaction();

        assertThat(transactions)
                .isNotNull()
                .hasSize(2)
                .containsExactlyElementsOf(expectedTransactions);
    }

    /**
     * Тест метода saveTransaction, который проверяет корректное сохранение транзакции в репозитории.
     */
    @Test
    public void testSaveTransaction() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        transactionService.setTransactionRepository(transactionRepository);

        Transaction transactionToSave = createTransaction(3);

        when(transactionRepository.saveTransaction(transactionToSave)).thenReturn(transactionToSave);

        Transaction savedTransaction = transactionService.saveTransaction(transactionToSave);

        assertThat(savedTransaction)
                .isNotNull()
                .isEqualTo(transactionToSave);
    }

    /**
     * Тест метода updateTransaction, который проверяет корректное обновление транзакции в репозитории.
     */
    @Test
    public void testUpdateTransaction() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        transactionService.setTransactionRepository(transactionRepository);

        int transactionId = 1;
        Transaction updatedTransaction = createTransaction(transactionId);
        updatedTransaction.setAmount(2000.0);

        when(transactionRepository.getTransactionById(transactionId)).thenReturn(updatedTransaction);

        transactionService.updateTransaction(updatedTransaction, transactionId);

        verify(transactionRepository).updateTransactionById(updatedTransaction, transactionId);
    }

    /**
     * Тест метода deleteTransaction, который проверяет корректное удаление транзакции из репозитория.
     */
    @Test
    public void testDeleteTransaction() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        transactionService.setTransactionRepository(transactionRepository);

        int transactionId = 1;
        Transaction transactionToDelete = createTransaction(transactionId);

        when(transactionRepository.getTransactionById(transactionId)).thenReturn(transactionToDelete);

        transactionService.deleteTransaction(transactionId);

        verify(transactionRepository).deleteTransactionById(transactionId);
    }

    /**
     * Тест метода createTransferTransactions, который проверяет корректное создание транзакций для перевода между счетами.
     */
    @Test
    public void testCreateTransferTransactions() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        transactionService.setTransactionRepository(transactionRepository);

        BankRepository bankRepository = mock(BankRepository.class);
        when(bankRepository.getBankById(anyInt())).thenReturn(
                Bank.builder()
                        .id(1)
                        .name("Source Bank")
                        .build(),
                Bank.builder()
                        .id(2)
                        .name("Target Bank")
                        .build()
        );
        transactionService.setBankRepository(bankRepository);

        Account sourceAccount = createAccount(1);
        Account targetAccount = createAccount(2);
        double amount = 500.0;

        transactionService.createTransferTransactions(sourceAccount, targetAccount, amount);

        verify(transactionRepository, times(2)).saveTransaction(any(Transaction.class));
        verify(documentGenerator).generateTransferCheck(anyDouble(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    /**
     * Тест метода saveDepositTransfer, который проверяет корректное сохранение транзакции депозита.
     */
    @Test
    public void testSaveDepositTransfer() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        transactionService.setTransactionRepository(transactionRepository);

        BankRepository bankRepository = mock(BankRepository.class);
        when(bankRepository.getBankById(anyInt())).thenReturn(Bank.builder()
                .id(1)
                .name("Test Bank")
                .build());
        transactionService.setBankRepository(bankRepository);

        Account account = createAccount(1);
        double amount = 500.0;

        transactionService.saveDepositTransfer(account, amount);

        verify(transactionRepository).saveTransaction(any(Transaction.class));
        verify(documentGenerator).generateCheck(anyDouble(), anyString(), anyString(), anyString());
    }

    /**
     * Тест метода saveWithdrawTransfer, который проверяет корректное сохранение транзакции снятия средств.
     */
    @Test
    public void testSaveWithdrawTransfer() {

        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        transactionService.setTransactionRepository(transactionRepository);

        BankRepository bankRepository = mock(BankRepository.class);
        when(bankRepository.getBankById(anyInt())).thenReturn(
                Bank.builder()
                        .id(1)
                        .name("Test Bank")
                        .build()
        );
        transactionService.setBankRepository(bankRepository);

        Account account = createAccount(1);
        double amount = 200.0;

        transactionService.saveWithdrawTransfer(account, amount);

        verify(transactionRepository).saveTransaction(any(Transaction.class));
        verify(documentGenerator).generateCheck(anyDouble(), anyString(), anyString(), anyString());
    }

    private Transaction createTransaction(int id) {
        return Transaction.builder()
                .id(id)
                .amount(1000.0)
                .sourceAccount(1)
                .targetAccount(2)
                .date(LocalDate.now())
                .type(TransactionType.TRANSFER_IN)
                .build();
    }

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
