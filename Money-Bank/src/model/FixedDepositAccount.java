package model;

import exception.InsufficientFundsException;
import exception.InvalidAmountException;

public class FixedDepositAccount extends Account implements InterestBearing {
    private static final long serialVersionUID = 1L;
    private static final double INTEREST_RATE = 0.07;

    public FixedDepositAccount(String ownerName, long balance) {
        super(ownerName, balance);
    }

    @Override
    public void withdraw(long amount)
            throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero.");
        }
        throw new InsufficientFundsException("Withdrawals are not allowed from this fixed deposit.");
    }

    @Override
    public double getAnnualInterestRate() {
        return INTEREST_RATE;
    }

    @Override
    public String toString() {
        return super.toString() + " | Annual interest: 7%";
    }
}
