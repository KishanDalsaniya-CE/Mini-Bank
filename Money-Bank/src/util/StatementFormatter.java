package util;

import java.util.List;

public final class StatementFormatter {
    private StatementFormatter() {}

    public static String format(String bankName, String accountNumber, List<String> transactions) {
        StringBuilder result = new StringBuilder();
        result.append("========== ").append(bankName).append(" ==========\n");
        result.append("Account Number: ").append(accountNumber).append("\n");
        result.append("----------------------------------------\n");
        if (transactions == null || transactions.isEmpty()) {
            result.append("No transactions available.\n");
        } else {
            for (String transaction : transactions) {
                result.append(transaction).append("\n");
            }
        }
        result.append("========================================\n");
        return result.toString();
    }
}
