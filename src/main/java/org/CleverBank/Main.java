package org.CleverBank;

import org.CleverBank.Repository.AccountRepository;
import org.CleverBank.Service.AccountService;

public class Main {
    public static void main(String[] args) {
        AccountService accountService=new AccountService(DatabaseUtil.getDataSource());
        accountService.deposit(accountService.getAccount(3),9);
        System.out.println(accountService.getAllAccounts());
    }
}
