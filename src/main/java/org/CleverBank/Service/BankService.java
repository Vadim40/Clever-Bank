package org.CleverBank.Service;

import lombok.Setter;
import org.CleverBank.Models.Bank;
import org.CleverBank.Repository.BankRepository;

import javax.sql.DataSource;
import java.util.List;

/**
 * Сервис для управления банками.
 */
@Setter
public class BankService {

    private BankRepository bankRepository;

    /**
     * Создает новый экземпляр класса `BankService`.
     *
     * @param dataSource Источник данных для взаимодействия с хранилищем банковских данных.
     */
    public BankService(DataSource dataSource) {
        this.bankRepository = new BankRepository(dataSource);
    }

    /**
     * Получает информацию о банке по его идентификатору.
     *
     * @param bankId Идентификатор банка.
     * @return Объект банка, если найден, в противном случае генерируется исключение.
     * @throws RuntimeException Если банк не найден.
     */
    public Bank getBank(int bankId) {
        Bank bank = bankRepository.getBankById(bankId);
        if (bank != null) {
            return bank;
        } else {
            throw new RuntimeException("Bank not found");
        }
    }

    /**
     * Получает список всех банков.
     *
     * @return Список банков.
     */
    public List<Bank> getAllBanks(){
        return bankRepository.getAllBanks();
    }

    /**
     * Сохраняет новый банк.
     *
     * @param bank Объект банка для сохранения.
     * @return Сохраненный объект банка.
     */
    public Bank saveBank(Bank bank) {
        return bankRepository.saveBank(bank);
    }

    /**
     * Обновляет информацию о банке.
     *
     * @param bank    Обновленный объект банка.
     * @param bankId  Идентификатор банка, который требуется обновить.
     */
    public void updateBank(Bank bank, int bankId) {
        bankRepository.updateBankById(bank, bankId);
    }

    /**
     * Удаляет банк по его идентификатору.
     *
     * @param bankId Идентификатор банка для удаления.
     * @throws RuntimeException Если банк не найден.
     */
    public void deleteBank(int bankId) {
        if (bankRepository.getBankById(bankId) != null) {
            bankRepository.deleteBankById(bankId);
        } else {
            throw new RuntimeException("Bank not found");
        }
    }
}
