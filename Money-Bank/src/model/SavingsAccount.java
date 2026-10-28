package model;

import exception.InsufficientFundsException;
import exception.InvalidAmountException;

public class SavingsAccount extends Account implements InterestBearing {
    private static final long serialVersionUID = 1L;

    private final long minimumBalance;
    private static final double INTEREST_RATE = 0.04;

    public SavingsAccount(String ownerName, long balance, long minimumBalance) {
        super(ownerName, balance);
        if (minimumBalance < 0 || balance < minimumBalance) {
            throw new IllegalArgumentException(
                    "Initial balance must be at least the non-negative minimum balance.");
        }
        this.minimumBalance = minimumBalance;
    }

    public long getMinimumBalance() {
        return minimumBalance;
    }

    @Override
    public synchronized void withdraw(long amount)
            throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero.");
        }
        if (balance - amount < minimumBalance) {
            throw new InsufficientFundsException("Withdrawal would break the minimum balance rule.");
        }
        balance -= amount;
    }

    @Override
    public double getAnnualInterestRate() {
        return INTEREST_RATE;
    }

    @Override
    public String toString() {
        return super.toString() + " | Minimum balance: ₹" + minimumBalance;
    }
}
