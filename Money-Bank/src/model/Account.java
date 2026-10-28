package model;

import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.annotation.Id;
import model.annotation.MaxLength;
import model.annotation.Positive;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Account implements Serializable, Comparable<Account>, Transactable {
    private static final long serialVersionUID = 1L;
    private static final AtomicLong ACCOUNT_COUNTER = new AtomicLong(1000);

    @Id
    private final String accountNumber;

    @MaxLength(30)
    private String ownerName;

    @Positive(message = "balance must be positive")
    protected long balance;

    protected Account(String ownerName, long balance) {
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Account holder name cannot be empty.");
        }
        if (ownerName.length() > 30) {
            throw new IllegalArgumentException("Account holder name cannot exceed 30 characters.");
        }
        if (balance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative.");
        }
        this.accountNumber = "AC" + ACCOUNT_COUNTER.incrementAndGet();
        this.ownerName = ownerName.trim();
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        if (ownerName == null || ownerName.isBlank() || ownerName.length() > 30) {
            throw new IllegalArgumentException("Name must contain 1 to 30 characters.");
        }
        this.ownerName = ownerName.trim();
    }

    public synchronized long getBalance() {
        return balance;
    }

    @Override
    public synchronized void deposit(long amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero.");
        }
        try {
            balance = Math.addExact(balance, amount);
        } catch (ArithmeticException e) {
            throw new InvalidAmountException("Deposit exceeds the supported balance limit.");
        }
    }

    public abstract void withdraw(long amount)
            throws InsufficientFundsException, InvalidAmountException;

    public abstract double getAnnualInterestRate();

    @Override
    public int compareTo(Account other) {
        return this.accountNumber.compareTo(other.accountNumber);
    }

    public static void ensureCounterAtLeast(long value) {
        ACCOUNT_COUNTER.accumulateAndGet(value, Math::max);
    }

    public static void updateCounterFromAccountNumber(String accountNumber) {
        if (accountNumber != null && accountNumber.matches("AC\\d+")) {
            try {
                ensureCounterAtLeast(Long.parseLong(accountNumber.substring(2)));
            } catch (NumberFormatException ignored) {
                // Ignore account numbers whose numeric suffix is too large.
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + " | Account: " + accountNumber
                + " | Holder: " + ownerName
                + " | Balance: ₹" + getBalance();
    }
}
