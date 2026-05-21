package chatapp;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatLogger {

    private static final String LOG_FILE = "chat_history.txt";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static synchronized void log(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry = "[" + timestamp + "] " + message;

        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            pw.println(logEntry);
        } catch (IOException e) {
            System.err.println("Logger Error: " + e.getMessage());
        }
    }

    public static void printHistory() {
        File file = new File(LOG_FILE);
        if (!file.exists()) {
            System.out.println("Koi chat history nahi mili abhi tak.");
            return;
        }
        System.out.println("\n========== CHAT HISTORY ==========");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("History read error: " + e.getMessage());
        }
        System.out.println("===================================\n");
    }

    public static void clearHistory() {
        try (FileWriter fw = new FileWriter(LOG_FILE, false)) {
            System.out.println("Chat history clear ho gayi!");
        } catch (IOException e) {
            System.err.println("Clear error: " + e.getMessage());
        }
    }
}