package project_2;


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

    public static String getHistory() {
        File file = new File(LOG_FILE);
        if (!file.exists()) return "No chat history found yet.";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            return "Error reading history: " + e.getMessage();
        }
        return sb.toString();
    }

    public static void clearHistory() {
        try (FileWriter fw = new FileWriter(LOG_FILE, false)) {
            fw.write("");
        } catch (IOException e) {
            System.err.println("Clear error: " + e.getMessage());
        }
    }
}