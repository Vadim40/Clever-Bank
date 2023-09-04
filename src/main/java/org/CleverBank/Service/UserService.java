package org.CleverBank.Service;

import lombok.Setter;
import org.CleverBank.Models.Account;
import org.CleverBank.Models.Transaction;
import org.CleverBank.Models.User;
import org.CleverBank.Repository.AccountRepository;
import org.CleverBank.Repository.TransactionRepository;
import org.CleverBank.Repository.UserRepository;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 * Сервисный класс `UserService` предоставляет функциональность для работы с пользователями, их транзакциями
 * и формирования выписок по счетам.
 */
@Setter
public class UserService {

    private UserRepository userRepository;
    private TransactionRepository transactionRepository;
    private AccountRepository accountRepository;
    private DocumentGenerator documentGenerator = new DocumentGenerator();

    /**
     * Конструктор класса `UserService`.
     *
     * @param dataSource Источник данных (DataSource), используемый для взаимодействия с базой данных.
     */
    public UserService(DataSource dataSource) {
        this.userRepository = new UserRepository(dataSource);
        transactionRepository = new TransactionRepository(dataSource);
        accountRepository = new AccountRepository(dataSource);
    }

    /**
     * Получает пользователя по идентификатору.
     *
     * @param userId Идентификатор пользователя.
     * @return Объект пользователя.
     * @throws RuntimeException, если пользователь не найден.
     */
    public User getUser(int userId) {
        User user = userRepository.getUserById(userId);
        if (user != null) {
            return user;
        } else {
            throw new RuntimeException("User not found");
        }
    }

    /**
     * Получает список всех пользователей.
     *
     * @return Список всех пользователей.
     */
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    /**
     * Генерирует выписку по счету пользователя за определенный период времени.
     *
     * @param userId    Идентификатор пользователя.
     * @param accountId Идентификатор счета.
     * @param startDate Начальная дата периода.
     * @param endDate   Конечная дата периода.
     * @return Строка с выпиской по счету пользователя.
     */
    public StringBuilder generateStatementForUser(int userId, int accountId,
                                                  LocalDate startDate, LocalDate endDate) {
        User user = userRepository.getUserById(userId);
        Account account = accountRepository.getAccountById(accountId);
        if (startDate == null) {
            startDate = account.getDate();
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        List<Transaction> transactions = transactionRepository.getAllTransactionsByAccountIdAndTime(
                accountId, startDate, endDate);
        List<StringBuilder> transactionsInfo = getTransactionsInfo(transactions);
        return documentGenerator.generateTransactionStatement(user, account, transactionsInfo);
    }

    /**
     * Получает информацию о транзакциях пользователя.
     *
     * @param transactions Список транзакций пользователя.
     * @return Список строк с информацией о транзакциях.
     */
    private List<StringBuilder> getTransactionsInfo(List<Transaction> transactions) {
        List<StringBuilder> transactionsInfo = new ArrayList<>();
        for (Transaction transaction : transactions) {
            Account account = accountRepository.getAccountById(transaction.getSourceAccount());
            User user = userRepository.getUserById(account.getUserId());
            transactionsInfo.add(documentGenerator.generateInfoAboutTransaction(transaction, user));
        }
        return transactionsInfo;
    }

    /**
     * Сохраняет нового пользователя.
     *
     * @param user Объект пользователя для сохранения.
     * @return Сохраненный объект пользователя.
     */
    public User saveUser(User user) {
        return userRepository.saveUser(user);
    }

    /**
     * Обновляет информацию о пользователе.
     *
     * @param user   Объект пользователя с обновленными данными.
     * @param userId Идентификатор пользователя для обновления.
     */
    public void updateUser(User user, int userId) {
        userRepository.updateUserById(user, userId);
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param userId Идентификатор пользователя для удаления.
     * @throws RuntimeException, если пользователь не найден.
     */
    public void deleteUser(int userId) {
        if (userRepository.getUserById(userId) != null) {
            userRepository.deleteUserById(userId);
        } else {
            throw new RuntimeException("User not found");
        }
    }
}
