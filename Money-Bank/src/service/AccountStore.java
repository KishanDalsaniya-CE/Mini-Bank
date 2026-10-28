package service;

import model.Account;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountStore {
    private final HashMap<String, Account> accounts = new HashMap<>();
    private final ArrayList<String> transactionHistory = new ArrayList<>();

    public synchronized void add(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null.");
        }
        String number = account.getAccountNumber();
        if (accounts.putIfAbsent(number, account) != null) {
            throw new IllegalArgumentException("Account already exists: " + number);
        }
    }

    public synchronized Account get(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public synchronized ArrayList<Account> getAll() {
        return new ArrayList<>(accounts.values());
    }

    public synchronized ArrayList<Account> sortedByBalance() {
        ArrayList<Account> result = getAll();
        result.sort(Comparator.comparingLong(Account::getBalance)
                .thenComparing(Account::getAccountNumber));
        return result;
    }

    public synchronized void addTransaction(String transaction) {
        if (transaction == null || transaction.isBlank()) {
            throw new IllegalArgumentException("Transaction cannot be empty.");
        }
        transactionHistory.add(transaction);
    }

    public synchronized ArrayList<String> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    public synchronized void replaceAll(Account[] loadedAccounts) {
        accounts.clear();
        if (loadedAccounts == null) {
            return;
        }
        for (Account account : loadedAccounts) {
            if (account != null) {
                accounts.put(account.getAccountNumber(), account);
                Account.updateCounterFromAccountNumber(account.getAccountNumber());
            }
        }
    }

    public synchronized int size() {
        return accounts.size();
    }

    public synchronized boolean contains(String accountNumber) {
        return accounts.containsKey(accountNumber);
    }
}
