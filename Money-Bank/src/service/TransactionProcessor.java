package service;

import exception.AccountNotFoundException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.Account;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionProcessor {
    private final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final Path logFile;

    public TransactionProcessor(Path logFile) {
        this.logFile = logFile;
    }

    public void addAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null.");
        }
        Account previous = accounts.putIfAbsent(account.getAccountNumber(), account);
        if (previous != null) {
            throw new IllegalArgumentException("Account already exists.");
        }
    }

    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public ConcurrentHashMap<String, Account> getAccountsSnapshot() {
        return new ConcurrentHashMap<>(accounts);
    }

    public void deposit(String accountNumber, long amount)
            throws AccountNotFoundException, InvalidAmountException, IOException {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        synchronized (account) {
            account.deposit(amount);
            TransactionLog.append(logFile,
                    "DEPOSIT," + accountNumber + "," + amount);
        }
    }

    public void withdraw(String accountNumber, long amount)
            throws AccountNotFoundException, InsufficientFundsException,
            InvalidAmountException, IOException {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        synchronized (account) {
            account.withdraw(amount);
            TransactionLog.append(logFile,
                    "WITHDRAW," + accountNumber + "," + amount);
        }
    }

    public void transfer(String fromNumber, String toNumber, long amount)
            throws AccountNotFoundException, InsufficientFundsException,
            InvalidAmountException, IOException {
        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be greater than zero.");
        }
        Account from = accounts.get(fromNumber);
        Account to = accounts.get(toNumber);
        if (from == null) {
            throw new AccountNotFoundException("Source account not found: " + fromNumber);
        }
        if (to == null) {
            throw new AccountNotFoundException("Destination account not found: " + toNumber);
        }
        if (from == to) {
            throw new InvalidAmountException("Source and destination accounts must differ.");
        }

        Account first = from.getAccountNumber().compareTo(to.getAccountNumber()) < 0 ? from : to;
        Account second = first == from ? to : from;

        synchronized (first) {
            synchronized (second) {
                from.withdraw(amount);
                try {
                    to.deposit(amount);
                } catch (InvalidAmountException e) {
                    try {
                        from.deposit(amount);
                    } catch (InvalidAmountException rollbackFailure) {
                        e.addSuppressed(rollbackFailure);
                    }
                    throw e;
                }

                try {
                    TransactionLog.append(logFile,
                            "TRANSFER_OUT," + fromNumber + "," + amount);
                    TransactionLog.append(logFile,
                            "TRANSFER_IN," + toNumber + "," + amount);
                } catch (IOException e) {
                    // Balances have changed; report the log failure to the caller.
                    throw e;
                }
            }
        }
    }
}
