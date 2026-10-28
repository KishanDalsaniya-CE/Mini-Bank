package model;

public interface InterestBearing {
    double getAnnualInterestRate();

    default double yearlyInterest(Account account) {
        return account.getBalance() * getAnnualInterestRate();
    }
}
