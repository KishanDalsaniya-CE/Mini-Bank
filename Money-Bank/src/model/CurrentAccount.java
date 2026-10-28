package model;

import exception.InsufficientFundsException;
import exception.InvalidAmountException;

public class CurrentAccount extends Account implements Premium {
    private static final long serialVersionUID = 1L;

    private final long overdraftLimit;

    public CurrentAccount(String ownerName, long balance, long overdraftLimit) {
        super(ownerName, balance);
        if (overdraftLimit < 0) {
            throw new IllegalArgumentException("Overdraft limit cannot be negative.");
        }
        this.overdraftLimit = overdraftLimit;
    }

    public long getOverdraftLimit() {
        return overdraftLimit;
    }

    @Override
    public synchronized void withdraw(long amount)
            throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero.");
        }
        if (balance - amount < -overdraftLimit) {
            throw new InsufficientFundsException("Overdraft limit exceeded.");
        }
        balance -= amount;
    }

    @Override
    public double getAnnualInterestRate() {
        return 0.0;
    }

    @Override
    public String toString() {
        return super.toString() + " | Overdraft limit: ₹" + overdraftLimit;
    }
}
