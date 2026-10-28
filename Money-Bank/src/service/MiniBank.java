package service;

import exception.AccountNotFoundException;
import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.Account;
import model.CurrentAccount;
import model.FixedDepositAccount;
import model.SavingsAccount;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

public class MiniBank {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final AccountStore STORE = new AccountStore();

    private static final Path DATA_FILE = Path.of("data", "accounts.dat");
    private static final Path LOG_FILE = Path.of("data", "logs", "transactions.log");
    private static final Path REPORT_FILE = Path.of("reports", "daily-report.txt");

    public static void main(String[] args) {
        loadAccounts();

        try (BankSession ignored = new BankSession()) {
            boolean running = true;

            while (running) {
                showMenu();
                int choice = readInt("Enter your choice: ");

                switch (choice) {
                    case 1 -> createAccount();
                    case 2 -> viewAllAccounts();
                    case 3 -> searchAccount();
                    case 4 -> sortAccounts();
                    case 5 -> depositMoney();
                    case 6 -> withdrawMoney();
                    case 7 -> transferMoney();
                    case 8 -> viewTransactionHistory();
                    case 9 -> generateReport();
                    case 10 -> saveAccounts();
                    case 11 -> running = false;
                    default -> System.out.println("Invalid choice. Please choose 1-11.");
                }
            }
        }

        saveAccounts();
        generateReport();
        SCANNER.close();
        System.out.println("Thank you for banking with Money Bank.");
    }

    private static void showMenu() {
        System.out.println("\n========== MONEY BANK ==========");
        System.out.println("1. Create Account");
        System.out.println("2. View All Accounts");
        System.out.println("3. Search Account");
        System.out.println("4. Sort Accounts by Balance");
        System.out.println("5. Deposit Money");
        System.out.println("6. Withdraw Money");
        System.out.println("7. Transfer Money");
        System.out.println("8. View Transaction History");
        System.out.println("9. Generate and Print Report");
        System.out.println("10. Save Accounts Now");
        System.out.println("11. Exit");
        System.out.println("================================");
    }

    private static void loadAccounts() {
        if (!Files.exists(DATA_FILE)) {
            System.out.println("Welcome to Money Bank, Kishan Dalsaniya.");
            return;
        }
        try {
            Account[] loaded = StatePersister.load(DATA_FILE);
            STORE.replaceAll(loaded);
            System.out.println("Loaded " + STORE.size() + " account(s) from saved data.");
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.out.println("Could not load saved accounts: " + e.getMessage());
        }
    }

    private static void createAccount() {
        System.out.println("\n----- CREATE ACCOUNT -----");
        System.out.println("1. Savings Account");
        System.out.println("2. Current Account");
        System.out.println("3. Fixed Deposit Account");
        int type = readInt("Select account type: ");

        if (type < 1 || type > 3) {
            System.out.println("Invalid account type.");
            return;
        }

        String name;
        while (true) {
            System.out.print("Enter account holder name: ");
            name = SCANNER.nextLine().trim();
            if (!name.isBlank() && name.length() <= 30) {
                break;
            }
            System.out.println("Name must contain 1-30 characters.");
        }

        long balance = readLong("Enter initial balance in rupees: ");
        if (balance < 0) {
            System.out.println("Initial balance cannot be negative.");
            return;
        }

        try {
            Account account;
            if (type == 1) {
                long minimum = readLong("Enter minimum balance: ");
                account = new SavingsAccount(name, balance, minimum);
            } else if (type == 2) {
                long overdraft = readLong("Enter overdraft limit: ");
                account = new CurrentAccount(name, balance, overdraft);
            } else {
                account = new FixedDepositAccount(name, balance);
            }

            STORE.add(account);
            record("ACCOUNT_CREATED," + account.getAccountNumber() + "," + balance);
            saveAccounts();

            System.out.println("Account created successfully.");
            System.out.println("Account number: " + account.getAccountNumber());
            System.out.println("Account holder: " + account.getOwnerName());
            System.out.println("Balance: ₹" + account.getBalance());
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Could not create account: " + e.getMessage());
        }
    }

    private static void viewAllAccounts() {
        System.out.println("\n----- ALL ACCOUNTS -----");
        ArrayList<Account> accounts = STORE.getAll();
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }
        accounts.forEach(System.out::println);
    }

    private static void searchAccount() {
        System.out.print("Enter account number: ");
        String number = SCANNER.nextLine().trim();
        Account account = STORE.get(number);
        if (account == null) {
            System.out.println("Account not found.");
        } else {
            System.out.println(account);
        }
    }

    private static void sortAccounts() {
        System.out.println("\n----- ACCOUNTS SORTED BY BALANCE -----");
        ArrayList<Account> accounts = STORE.sortedByBalance();
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }
        accounts.forEach(System.out::println);
    }

    private static void depositMoney() {
        Account account = findAccount();
        if (account == null) return;
        long amount = readLong("Enter deposit amount: ");

        try {
            synchronized (account) {
                account.deposit(amount);
                record("DEPOSIT," + account.getAccountNumber() + "," + amount);
            }
            saveAccounts();
            System.out.println("Deposit successful. New balance: ₹" + account.getBalance());
        } catch (InvalidAmountException | IOException e) {
            System.out.println("Deposit failed: " + e.getMessage());
        }
    }

    private static void withdrawMoney() {
        Account account = findAccount();
        if (account == null) return;
        long amount = readLong("Enter withdrawal amount: ");

        try {
            synchronized (account) {
                account.withdraw(amount);
                record("WITHDRAW," + account.getAccountNumber() + "," + amount);
            }
            saveAccounts();
            System.out.println("Withdrawal successful. New balance: ₹" + account.getBalance());
        } catch (InsufficientFundsException | InvalidAmountException | IOException e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
        }
    }

    private static void transferMoney() {
        Account from = findAccountWithPrompt("Enter source account number: ");
        if (from == null) return;
        Account to = findAccountWithPrompt("Enter destination account number: ");
        if (to == null) return;
        long amount = readLong("Enter transfer amount: ");

        // Use the same ordered locking pattern as the concurrent processor.
        Account first = from.getAccountNumber().compareTo(to.getAccountNumber()) < 0 ? from : to;
        Account second = first == from ? to : from;

        try {
            synchronized (first) {
                synchronized (second) {
                    from.withdraw(amount);
                    try {
                        to.deposit(amount);
                    } catch (InvalidAmountException e) {
                        from.deposit(amount);
                        throw e;
                    }
                    record("TRANSFER_OUT," + from.getAccountNumber() + "," + amount);
                    record("TRANSFER_IN," + to.getAccountNumber() + "," + amount);
                }
            }
            saveAccounts();
            System.out.println("Transfer successful.");
        } catch (BankException | IOException e) {
            System.out.println("Transfer failed: " + e.getMessage());
        }
    }

    private static Account findAccount() {
        return findAccountWithPrompt("Enter account number: ");
    }

    private static Account findAccountWithPrompt(String prompt) {
        System.out.print(prompt);
        String number = SCANNER.nextLine().trim();
        Account account = STORE.get(number);
        if (account == null) {
            System.out.println("Account not found.");
        }
        return account;
    }

    private static void viewTransactionHistory() {
        System.out.println("\n----- TRANSACTION HISTORY (CURRENT SESSION) -----");
        ArrayList<String> transactions = STORE.getTransactionHistory();
        if (transactions.isEmpty()) {
            System.out.println("No transactions recorded in this session.");
            System.out.println("Full persistent history is in " + LOG_FILE);
            return;
        }
        transactions.forEach(System.out::println);
    }

    private static void generateReport() {
        try {
            ReportGenerator.generate(LOG_FILE.getParent(), REPORT_FILE);
            ReportGenerator.printReport(REPORT_FILE);
        } catch (IOException | ArithmeticException e) {
            System.out.println("Could not generate report: " + e.getMessage());
        }
    }

    private static void saveAccounts() {
        try {
            StatePersister.save(STORE.getAll().toArray(new Account[0]), DATA_FILE);
        } catch (IOException e) {
            System.out.println("Could not save accounts: " + e.getMessage());
        }
    }

    private static void record(String transaction) throws IOException {
        STORE.addTransaction(transaction);
        TransactionLog.append(LOG_FILE, transaction);
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number amount.");
            }
        }
    }
}
