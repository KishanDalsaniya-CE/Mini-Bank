package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ReportGenerator {
    private ReportGenerator() {}

    public static void generate(Path logsFolder, Path reportFile) throws IOException {
        long deposits = 0;
        long withdrawals = 0;
        List<String> details = new ArrayList<>();

        if (Files.isDirectory(logsFolder)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(logsFolder)) {
                for (Path file : stream) {
                    if (!Files.isRegularFile(file)) {
                        continue;
                    }

                    BasicFileAttributes attrs =
                            Files.readAttributes(file, BasicFileAttributes.class);
                    details.add(file.getFileName() + " | " + attrs.size()
                            + " bytes | Modified: " + attrs.lastModifiedTime());

                    try (BufferedReader reader = Files.newBufferedReader(file)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // Log format: timestamp,TYPE,ACCOUNT_NUMBER,AMOUNT
                            String[] parts = line.split(",");
                            if (parts.length < 4) {
                                continue;
                            }
                            try {
                                long amount = Long.parseLong(parts[3].trim());
                                String type = parts[1].trim().toUpperCase();

                                if (amount < 0) {
                                    continue;
                                }
                                if (type.equals("DEPOSIT") || type.equals("TRANSFER_IN")) {
                                    deposits = Math.addExact(deposits, amount);
                                } else if (type.equals("WITHDRAW")
                                        || type.equals("TRANSFER_OUT")) {
                                    withdrawals = Math.addExact(withdrawals, amount);
                                }
                            } catch (NumberFormatException ignored) {
                                // Skip malformed transaction lines.
                            }
                        }
                    }
                }
            }
        }

        long net = Math.subtractExact(deposits, withdrawals);
        StringBuilder report = new StringBuilder();
        report.append("============== MONEY BANK ==============\n");
        report.append("Transaction Summary Report\n");
        report.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        report.append("Total deposits / transfer-ins: ₹").append(deposits).append("\n");
        report.append("Total withdrawals / transfer-outs: ₹").append(withdrawals).append("\n");
        report.append("Net transaction flow: ₹").append(net).append("\n\n");
        report.append("Log file details:\n");

        if (details.isEmpty()) {
            report.append("No log files found.\n");
        } else {
            for (String detail : details) {
                report.append(detail).append("\n");
            }
        }
        report.append("========================================\n");

        Path parent = reportFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(reportFile, report.toString());
    }

    public static void printReport(Path reportFile) throws IOException {
        if (Files.exists(reportFile)) {
            System.out.println(Files.readString(reportFile));
        } else {
            System.out.println("Report file does not exist.");
        }
    }
}
