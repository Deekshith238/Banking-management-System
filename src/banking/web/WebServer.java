package banking.web;

import banking.model.*;
import banking.service.*;
import banking.util.DBConnection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Embedded HTTP REST Server for the Banking Management System.
 * Serves static web assets (HTML/CSS/JS) and provides JSON API endpoints.
 */
public class WebServer {
    private static final int PORT = 8080;
    private static CustomerService customerService;
    private static AccountService accountService;
    private static TransactionService transactionService;

    // In-memory fallback repositories (used when MySQL is offline)
    private static final List<Customer> fallbackCustomers = new ArrayList<>();
    private static final List<Account> fallbackAccounts = new ArrayList<>();
    private static final List<Transaction> fallbackTransactions = new ArrayList<>();
    private static final AtomicInteger customerIdSeq = new AtomicInteger(100);
    private static final AtomicInteger txIdSeq = new AtomicInteger(500);

    static {
        // Populate fallback sample data
        initFallbackData();
    }

    public static void main(String[] args) {
        startServer();
    }

    public static void startServer() {
        try {
            customerService = new CustomerService();
            accountService = new AccountService();
            transactionService = new TransactionService();

            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/", new StaticFileHandler());
            server.createContext("/api/stats", new StatsHandler());
            server.createContext("/api/customers", new CustomerHandler());
            server.createContext("/api/accounts", new AccountHandler());
            server.createContext("/api/deposit", new DepositHandler());
            server.createContext("/api/withdraw", new WithdrawHandler());
            server.createContext("/api/transfer", new TransferHandler());
            server.createContext("/api/transactions", new TransactionHandler());
            server.createContext("/api/db-status", new DBStatusHandler());

            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();

            System.out.println("==========================================================");
            System.out.println("🚀 BANKING SYSTEM WEB FRONTEND IS ONLINE!");
            System.out.println("👉 Access UI Dashboard at: http://localhost:" + PORT);
            System.out.println("==========================================================");
        } catch (IOException e) {
            System.err.println("❌ Failed to start WebServer on port " + PORT + ": " + e.getMessage());
        }
    }

    private static void initFallbackData() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Customer c1 = new Customer(101, "Rajesh Kumar", "rajesh.kumar@example.in", "+91 98765 43210", "12, MG Road, Indiranagar, Bengaluru, Karnataka 560038", now);
        Customer c2 = new Customer(102, "Priya Sharma", "priya.sharma@example.in", "+91 91234 56789", "45, Connaught Place, New Delhi 110001", now);
        Customer c3 = new Customer(103, "Aarav Patel", "aarav.patel@example.in", "+91 99887 76655", "88, Bandra West, Mumbai, Maharashtra 400050", now);
        fallbackCustomers.add(c1);
        fallbackCustomers.add(c2);
        fallbackCustomers.add(c3);

        SavingsAccount sa1 = new SavingsAccount("SAV1001", 101, 125000.00);
        sa1.setCreatedAt(now);
        CurrentAccount ca1 = new CurrentAccount("CUR1002", 101, 250000.00);
        ca1.setCreatedAt(now);
        SavingsAccount sa2 = new SavingsAccount("SAV2001", 102, 340500.50);
        sa2.setCreatedAt(now);
        CurrentAccount ca3 = new CurrentAccount("CUR3001", 103, 85000.00);
        ca3.setCreatedAt(now);

        fallbackAccounts.add(sa1);
        fallbackAccounts.add(ca1);
        fallbackAccounts.add(sa2);
        fallbackAccounts.add(ca3);

        fallbackTransactions.add(new Transaction(501, "SAV1001", "DEPOSIT", 125000.00, 125000.00, null, "Initial Account Opening Deposit", now));
        fallbackTransactions.add(new Transaction(502, "CUR1002", "DEPOSIT", 300000.00, 300000.00, null, "Initial Business Deposit", now));
        fallbackTransactions.add(new Transaction(503, "CUR1002", "WITHDRAWAL", 50000.00, 250000.00, null, "ATM Cash Withdrawal", now));
        fallbackTransactions.add(new Transaction(504, "SAV2001", "DEPOSIT", 340500.50, 340500.50, null, "Salary Credit", now));
        fallbackTransactions.add(new Transaction(505, "CUR3001", "DEPOSIT", 85000.00, 85000.00, null, "UPI Transfer Received", now));
    }

    private static boolean isDbAvailable() {
        return DBConnection.testConnection();
    }

    // =========================================================================
    // HTTP HANDLERS
    // =========================================================================

    /** Static File Handler serving web/ index.html, styles.css, app.js */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String pathStr = exchange.getRequestURI().getPath();
            if (pathStr.equals("/")) {
                pathStr = "/index.html";
            }

            Path filePath = Paths.get("web", pathStr);
            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                sendResponse(exchange, 404, "text/plain", "404 Not Found");
                return;
            }

            String contentType = "text/html";
            if (pathStr.endsWith(".css")) contentType = "text/css";
            else if (pathStr.endsWith(".js")) contentType = "application/javascript";
            else if (pathStr.endsWith(".json")) contentType = "application/json";
            else if (pathStr.endsWith(".png")) contentType = "image/png";
            else if (pathStr.endsWith(".svg")) contentType = "image/svg+xml";

            byte[] bytes = Files.readAllBytes(filePath);
            sendResponse(exchange, 200, contentType, bytes);
        }
    }

    /** GET /api/stats */
    static class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            int customerCount = 0;
            int accountCount = 0;
            double totalBalance = 0.0;
            int txCount = 0;
            boolean dbConnected = isDbAvailable();

            if (dbConnected) {
                try {
                    List<Customer> customers = customerService.getAllCustomers();
                    List<Account> accounts = accountService.getAllAccounts();
                    List<Transaction> txs = transactionService.getAllTransactions();
                    customerCount = customers.size();
                    accountCount = accounts.size();
                    txCount = txs.size();
                    for (Account acc : accounts) {
                        totalBalance += acc.getBalance();
                    }
                } catch (Exception e) {
                    dbConnected = false;
                }
            }

            if (!dbConnected) {
                customerCount = fallbackCustomers.size();
                accountCount = fallbackAccounts.size();
                txCount = fallbackTransactions.size();
                for (Account acc : fallbackAccounts) {
                    totalBalance += acc.getBalance();
                }
            }

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"dbConnected\":").append(dbConnected).append(",");
            json.append("\"customerCount\":").append(customerCount).append(",");
            json.append("\"accountCount\":").append(accountCount).append(",");
            json.append("\"totalBalance\":").append(String.format(Locale.US, "%.2f", totalBalance)).append(",");
            json.append("\"txCount\":").append(txCount);
            json.append("}");

            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    /** GET /api/customers & POST /api/customers */
    static class CustomerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                List<Customer> customers = new ArrayList<>();
                boolean dbConnected = isDbAvailable();
                if (dbConnected) {
                    try {
                        customers = customerService.getAllCustomers();
                    } catch (Exception e) {
                        dbConnected = false;
                    }
                }
                if (!dbConnected) {
                    customers = fallbackCustomers;
                }

                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < customers.size(); i++) {
                    Customer c = customers.get(i);
                    json.append(customerToJson(c));
                    if (i < customers.size() - 1) json.append(",");
                }
                json.append("]");
                sendResponse(exchange, 200, "application/json", json.toString());

            } else if ("POST".equalsIgnoreCase(method)) {
                Map<String, String> body = parseJsonBody(readRequestBody(exchange));
                String name = body.get("name");
                String email = body.get("email");
                String phone = body.get("phone");
                String address = body.get("address");

                if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty()) {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"Name and email are required\"}");
                    return;
                }

                boolean dbConnected = isDbAvailable();
                if (dbConnected) {
                    try {
                        Customer newCust = customerService.registerCustomer(name, email, phone, address);
                        sendResponse(exchange, 201, "application/json", customerToJson(newCust));
                        return;
                    } catch (Exception e) {
                        sendResponse(exchange, 400, "application/json", "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                        return;
                    }
                }

                // Fallback implementation
                int newId = customerIdSeq.incrementAndGet();
                Timestamp now = new Timestamp(System.currentTimeMillis());
                Customer c = new Customer(newId, name, email, phone != null ? phone : "", address != null ? address : "", now);
                fallbackCustomers.add(c);
                sendResponse(exchange, 201, "application/json", customerToJson(c));
            } else {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
            }
        }
    }

    /** GET /api/accounts & POST /api/accounts */
    static class AccountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                List<Account> accounts = new ArrayList<>();
                boolean dbConnected = isDbAvailable();
                if (dbConnected) {
                    try {
                        accounts = accountService.getAllAccounts();
                    } catch (Exception e) {
                        dbConnected = false;
                    }
                }
                if (!dbConnected) {
                    accounts = fallbackAccounts;
                }

                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < accounts.size(); i++) {
                    Account a = accounts.get(i);
                    json.append(accountToJson(a));
                    if (i < accounts.size() - 1) json.append(",");
                }
                json.append("]");
                sendResponse(exchange, 200, "application/json", json.toString());

            } else if ("POST".equalsIgnoreCase(method)) {
                Map<String, String> body = parseJsonBody(readRequestBody(exchange));
                String customerIdStr = body.get("customerId");
                String accountType = body.get("accountType");
                String depositStr = body.get("initialDeposit");

                try {
                    int customerId = Integer.parseInt(customerIdStr);
                    double initialDeposit = Double.parseDouble(depositStr);

                    boolean dbConnected = isDbAvailable();
                    if (dbConnected) {
                        Account acc = accountService.openAccount(customerId, accountType, initialDeposit);
                        sendResponse(exchange, 201, "application/json", accountToJson(acc));
                        return;
                    }

                    // Fallback create
                    String prefix = "SAVINGS".equalsIgnoreCase(accountType) ? "SAV" : "CUR";
                    String accNum = prefix + (1000 + new Random().nextInt(9000));
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    Account acc;
                    if ("SAVINGS".equalsIgnoreCase(accountType)) {
                        if (initialDeposit < SavingsAccount.MINIMUM_BALANCE) {
                            sendResponse(exchange, 400, "application/json",
                                    "{\"error\":\"Initial deposit for Savings Account must be at least $" + SavingsAccount.MINIMUM_BALANCE + "\"}");
                            return;
                        }
                        acc = new SavingsAccount(accNum, customerId, initialDeposit);
                    } else {
                        acc = new CurrentAccount(accNum, customerId, initialDeposit);
                    }
                    acc.setCreatedAt(now);
                    fallbackAccounts.add(acc);

                    if (initialDeposit > 0) {
                        fallbackTransactions.add(new Transaction(txIdSeq.incrementAndGet(), accNum, "DEPOSIT", initialDeposit, initialDeposit, null, "Initial Account Opening Deposit", now));
                    }
                    sendResponse(exchange, 201, "application/json", accountToJson(acc));

                } catch (Exception e) {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
            }
        }
    }

    /** POST /api/deposit */
    static class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            Map<String, String> body = parseJsonBody(readRequestBody(exchange));
            String accNo = body.get("accountNumber");
            String amtStr = body.get("amount");
            String remarks = body.get("remarks");

            try {
                double amount = Double.parseDouble(amtStr);
                if (amount <= 0) {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"Deposit amount must be greater than 0\"}");
                    return;
                }

                boolean dbConnected = isDbAvailable();
                if (dbConnected) {
                    boolean success = accountService.deposit(accNo, amount, remarks);
                    if (success) {
                        Account acc = accountService.getAccount(accNo);
                        sendResponse(exchange, 200, "application/json", "{\"success\":true, \"account\":" + accountToJson(acc) + "}");
                    } else {
                        sendResponse(exchange, 400, "application/json", "{\"error\":\"Deposit failed\"}");
                    }
                    return;
                }

                // Fallback Deposit
                Account acc = findFallbackAccount(accNo);
                if (acc == null) {
                    sendResponse(exchange, 404, "application/json", "{\"error\":\"Account number not found\"}");
                    return;
                }
                acc.deposit(amount);
                Timestamp now = new Timestamp(System.currentTimeMillis());
                Transaction tx = new Transaction(txIdSeq.incrementAndGet(), accNo, "DEPOSIT", amount, acc.getBalance(), null, remarks != null ? remarks : "Deposit", now);
                fallbackTransactions.add(tx);

                sendResponse(exchange, 200, "application/json", "{\"success\":true, \"account\":" + accountToJson(acc) + "}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    /** POST /api/withdraw */
    static class WithdrawHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            Map<String, String> body = parseJsonBody(readRequestBody(exchange));
            String accNo = body.get("accountNumber");
            String amtStr = body.get("amount");
            String remarks = body.get("remarks");

            try {
                double amount = Double.parseDouble(amtStr);
                if (amount <= 0) {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"Withdrawal amount must be greater than 0\"}");
                    return;
                }

                boolean dbConnected = isDbAvailable();
                if (dbConnected) {
                    boolean success = accountService.withdraw(accNo, amount, remarks);
                    if (success) {
                        Account acc = accountService.getAccount(accNo);
                        sendResponse(exchange, 200, "application/json", "{\"success\":true, \"account\":" + accountToJson(acc) + "}");
                    } else {
                        sendResponse(exchange, 400, "application/json", "{\"error\":\"Withdrawal failed\"}");
                    }
                    return;
                }

                // Fallback Withdrawal
                Account acc = findFallbackAccount(accNo);
                if (acc == null) {
                    sendResponse(exchange, 404, "application/json", "{\"error\":\"Account number not found\"}");
                    return;
                }

                if (!acc.canWithdraw(amount)) {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"Insufficient balance or overdraft limit exceeded!\"}");
                    return;
                }

                acc.withdraw(amount);
                Timestamp now = new Timestamp(System.currentTimeMillis());
                Transaction tx = new Transaction(txIdSeq.incrementAndGet(), accNo, "WITHDRAWAL", amount, acc.getBalance(), null, remarks != null ? remarks : "Withdrawal", now);
                fallbackTransactions.add(tx);

                sendResponse(exchange, 200, "application/json", "{\"success\":true, \"account\":" + accountToJson(acc) + "}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    /** POST /api/transfer */
    static class TransferHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            Map<String, String> body = parseJsonBody(readRequestBody(exchange));
            String fromAcc = body.get("fromAccount");
            String toAcc = body.get("toAccount");
            String amtStr = body.get("amount");
            String remarks = body.get("remarks");

            try {
                double amount = Double.parseDouble(amtStr);
                if (fromAcc == null || toAcc == null || fromAcc.equalsIgnoreCase(toAcc)) {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"Source and destination accounts must be distinct\"}");
                    return;
                }
                if (amount <= 0) {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"Transfer amount must be greater than 0\"}");
                    return;
                }

                boolean dbConnected = isDbAvailable();
                if (dbConnected) {
                    boolean success = accountService.transferFunds(fromAcc, toAcc, amount, remarks);
                    if (success) {
                        sendResponse(exchange, 200, "application/json", "{\"success\":true, \"message\":\"Transfer successful\"}");
                    } else {
                        sendResponse(exchange, 400, "application/json", "{\"error\":\"Transfer failed\"}");
                    }
                    return;
                }

                // Fallback Transfer
                Account src = findFallbackAccount(fromAcc);
                Account dst = findFallbackAccount(toAcc);
                if (src == null || dst == null) {
                    sendResponse(exchange, 404, "application/json", "{\"error\":\"Source or Destination account not found\"}");
                    return;
                }

                if (!src.canWithdraw(amount)) {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"Insufficient funds in source account\"}");
                    return;
                }

                src.withdraw(amount);
                dst.deposit(amount);
                Timestamp now = new Timestamp(System.currentTimeMillis());

                String rem = remarks != null && !remarks.trim().isEmpty() ? remarks : "Fund Transfer";
                fallbackTransactions.add(new Transaction(txIdSeq.incrementAndGet(), fromAcc, "TRANSFER_OUT", amount, src.getBalance(), toAcc, rem, now));
                fallbackTransactions.add(new Transaction(txIdSeq.incrementAndGet(), toAcc, "TRANSFER_IN", amount, dst.getBalance(), fromAcc, rem, now));

                sendResponse(exchange, 200, "application/json", "{\"success\":true, \"message\":\"Transfer completed successfully\"}");
            } catch (Exception e) {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    /** GET /api/transactions?accountNumber=... */
    static class TransactionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String accountNumber = params.get("accountNumber");

            List<Transaction> txList = new ArrayList<>();
            boolean dbConnected = isDbAvailable();

            if (dbConnected) {
                try {
                    if (accountNumber != null && !accountNumber.trim().isEmpty()) {
                        txList = transactionService.getAccountStatement(accountNumber.trim());
                    } else {
                        txList = transactionService.getAllTransactions();
                    }
                } catch (Exception e) {
                    dbConnected = false;
                }
            }

            if (!dbConnected) {
                if (accountNumber != null && !accountNumber.trim().isEmpty()) {
                    for (Transaction tx : fallbackTransactions) {
                        if (tx.getAccountNumber().equalsIgnoreCase(accountNumber.trim())) {
                            txList.add(tx);
                        }
                    }
                } else {
                    txList = fallbackTransactions;
                }
            }

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < txList.size(); i++) {
                Transaction tx = txList.get(i);
                json.append(transactionToJson(tx));
                if (i < txList.size() - 1) json.append(",");
            }
            json.append("]");

            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    /** GET /api/db-status & POST /api/db-status */
    static class DBStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                boolean connected = DBConnection.testConnection();
                String json = String.format(Locale.US,
                        "{\"connected\":%b, \"url\":\"%s\", \"user\":\"%s\"}",
                        connected, escapeJson(DBConnection.getUrl()), escapeJson(DBConnection.getUser()));
                sendResponse(exchange, 200, "application/json", json);
            } else if ("POST".equalsIgnoreCase(method)) {
                Map<String, String> body = parseJsonBody(readRequestBody(exchange));
                String url = body.get("url");
                String user = body.get("user");
                String password = body.get("password");

                if (url != null && user != null && password != null) {
                    DBConnection.setCredentials(url, user, password);
                }
                boolean connected = DBConnection.testConnection();
                String json = String.format(Locale.US,
                        "{\"connected\":%b, \"url\":\"%s\", \"user\":\"%s\"}",
                        connected, escapeJson(DBConnection.getUrl()), escapeJson(DBConnection.getUser()));
                sendResponse(exchange, 200, "application/json", json);
            } else {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
            }
        }
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private static Account findFallbackAccount(String accNo) {
        for (Account a : fallbackAccounts) {
            if (a.getAccountNumber().equalsIgnoreCase(accNo)) {
                return a;
            }
        }
        return null;
    }

    private static String customerToJson(Customer c) {
        if (c == null) return "{}";
        return String.format(Locale.US,
                "{\"customerId\":%d, \"name\":\"%s\", \"email\":\"%s\", \"phone\":\"%s\", \"address\":\"%s\", \"createdAt\":\"%s\"}",
                c.getCustomerId(), escapeJson(c.getName()), escapeJson(c.getEmail()),
                escapeJson(c.getPhone()), escapeJson(c.getAddress()),
                c.getCreatedAt() != null ? c.getCreatedAt().toString() : "");
    }

    private static String accountToJson(Account a) {
        if (a == null) return "{}";
        return String.format(Locale.US,
                "{\"accountNumber\":\"%s\", \"customerId\":%d, \"accountType\":\"%s\", \"balance\":%.2f, \"createdAt\":\"%s\"}",
                escapeJson(a.getAccountNumber()), a.getCustomerId(), escapeJson(a.getAccountType()),
                a.getBalance(), a.getCreatedAt() != null ? a.getCreatedAt().toString() : "");
    }

    private static String transactionToJson(Transaction t) {
        if (t == null) return "{}";
        return String.format(Locale.US,
                "{\"transactionId\":%d, \"accountNumber\":\"%s\", \"transactionType\":\"%s\", \"amount\":%.2f, \"balanceAfter\":%.2f, \"targetAccountNumber\":\"%s\", \"remarks\":\"%s\", \"timestamp\":\"%s\"}",
                t.getTransactionId(), escapeJson(t.getAccountNumber()), escapeJson(t.getTransactionType()),
                t.getAmount(), t.getBalanceAfter(),
                escapeJson(t.getTargetAccountNumber() != null ? t.getTargetAccountNumber() : ""),
                escapeJson(t.getRemarks() != null ? t.getRemarks() : ""),
                t.getTimestamp() != null ? t.getTimestamp().toString() : "");
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseJsonBody(String jsonStr) {
        Map<String, String> map = new HashMap<>();
        if (jsonStr == null || jsonStr.trim().isEmpty()) return map;

        String clean = jsonStr.trim();
        if (clean.startsWith("{")) clean = clean.substring(1);
        if (clean.endsWith("}")) clean = clean.substring(0, clean.length() - 1);

        String[] pairs = clean.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replaceAll("^\"|\"$", "");
                String val = kv[1].trim().replaceAll("^\"|\"$", "");
                map.put(key, val);
            }
        }
        return map;
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                params.put(entry[0], entry[1]);
            } else if (entry.length == 1) {
                params.put(entry[0], "");
            }
        }
        return params;
    }

    private static void sendResponse(HttpExchange exchange, int status, String contentType, String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        sendResponse(exchange, status, contentType, bytes);
    }

    private static void sendResponse(HttpExchange exchange, int status, String contentType, byte[] bytes) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
