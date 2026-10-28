# Money Bank — Java Console Banking Project

A Java 17 console banking project using the Collections Framework, OOP, exceptions,
serialization, transaction logging, reporting, and concurrent account storage.

## Branding
- Bank name: Money Bank
- Example account holder: Kishan Dalsaniya

## Requirements
- JDK 17 or newer
- VS Code, IntelliJ IDEA, Eclipse, or a terminal

## Project structure
See `PROJECT_STRUCTURE.txt`.

## Compile and run (from the Money-Bank folder)

### Windows PowerShell / Command Prompt
```bat
javac -d out src\model\*.java src\model\annotation\*.java src\exception\*.java src\util\*.java src\service\*.java
java -cp out service.MiniBank
```

### macOS / Linux
```bash
javac -d out src/model/*.java src/model/annotation/*.java src/exception/*.java src/util/*.java src/service/*.java
java -cp out service.MiniBank
```

The application loads `data/accounts.dat` if it exists. It saves accounts on exit.
Transactions are appended to `data/logs/transactions.log`. A report is generated at
`reports/daily-report.txt` when you choose the report option or exit.

## Menu
1. Create account
2. View all accounts
3. Search account
4. Sort accounts by balance
5. Deposit money
6. Withdraw money
7. Transfer money
8. View transaction history
9. Generate and print report
10. Save accounts now
11. Exit

## Notes
- Account balances are represented as whole rupees (`long`) for this learning project.
- `HashMap` does not guarantee display order. Use the sorting menu for ordered results.
- `ConcurrentHashMap` protects the shared map structure; account operations are synchronized
  on each account object as well.
- Java serialization is suitable for this classroom demo, not a production banking system.
- The transaction report totals all lines in the log file, so totals accumulate over runs.
