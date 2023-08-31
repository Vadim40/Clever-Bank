package org.CleverBank.Service;

import org.CleverBank.Models.Bank;
import org.CleverBank.Repository.BankRepository;

import javax.sql.DataSource;

public class BankService {
    private BankRepository bankRepository;

    public BankService(DataSource dataSource) {
        this.bankRepository = new BankRepository(dataSource);
    }

    public Bank getBank(int bankId) {
        Bank bank = bankRepository.getBankById(bankId);
        if (bank != null) {
            return bank;
        } else {
            throw new RuntimeException("Bank not found");
        }
    }

    public Bank saveBank(Bank bank) {

        return bankRepository.saveBank(bank);
    }

    public void updateBank(Bank bank, int bankId) {

        bankRepository.updateBankById(bank, bankId);
    }

    public void deleteBank(int bankId) {
        if (bankRepository.getBankById(bankId) != null) {
            bankRepository.deleteBankById(bankId);
        } else {
            throw new RuntimeException("Bank not found");
        }
    }
}
