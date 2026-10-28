package model;

@FunctionalInterface
public interface WithdrawRule {
    boolean canWithdraw(Account account, long amount);
}
