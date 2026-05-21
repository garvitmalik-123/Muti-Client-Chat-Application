package chatapp;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    private static final int PORT = 5000;
    private static Map<String, PrintWriter> activeClients = new HashMap<>();

    // ─── ANSI Color Codes ───────────────────────────────────────────
    static final String RESET   = "\u001B[0m";
    static final String BOLD    = "\u001B[1m";
    static final String RED     = "\u001B[31m";
    static final String GREEN   = "\u001B[32m";
    static final String YELLOW  = "\u001B[33m";
    static final String BLUE    = "\u001B[34m";
    static final String MAGENTA = "\u001B[35m";
    static final String CYAN    = "\u001B[36m";
    static final String WHITE   = "\u001B[37m";
    static final String BG_DARK = "\u001B[40m";
    // ────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        printServerBanner();
        ChatLogger.log("=== SERVER STARTED ===");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log(GREEN, "READY", "Waiting for clients on port " + PORT + "...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
            }
        } catch (IOException e) {
            log(RED, "ERROR", e.getMessage());
        }
    }

    // ─── Formatted Log ──────────────────────────────────────────────
    static void log(String color, String tag, String msg) {
        System.out.println(color + BOLD + "[" + tag + "]" + RESET + " " + msg);
    }

    // ─── Server Banner ──────────────────────────────────────────────
    static void printServerBanner() {
        System.out.println();
        System.out.println(CYAN + BOLD + "  ╔══════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + BOLD + "  ║                                                  ║" + RESET);
        System.out.println(CYAN + BOLD + "  ║" + YELLOW + BOLD + "        ░░░  JavaChat Server  ░░░               " + CYAN + "  ║" + RESET);
        System.out.println(CYAN + BOLD + "  ║" + WHITE  + "          Multi-Client   |   Core Java          " + CYAN + "  ║" + RESET);
        System.out.println(CYAN + BOLD + "  ║" + GREEN  + "              PORT: 5000                        " + CYAN + "  ║" + RESET);
        System.out.println(CYAN + BOLD + "  ║                                                  ║" + RESET);
        System.out.println(CYAN + BOLD + "  ╠══════════════════════════════════════════════════╣" + RESET);
        System.out.println(CYAN + BOLD + "  ║  " + WHITE + "Features:" + RESET);
        System.out.println(CYAN + BOLD + "  ║  " + GREEN + "  ✔  " + WHITE + "Multi-client support (Threads)" + RESET);
        System.out.println(CYAN + BOLD + "  ║  " + GREEN + "  ✔  " + WHITE + "Group broadcast messaging" + RESET);
        System.out.println(CYAN + BOLD + "  ║  " + GREEN + "  ✔  " + WHITE + "Private messaging (/pm)" + RESET);
        System.out.println(CYAN + BOLD + "  ║  " + GREEN + "  ✔  " + WHITE + "Chat history saved to file" + RESET);
        System.out.println(CYAN + BOLD + "  ║  " + GREEN + "  ✔  " + WHITE + "Online user list (/list)" + RESET);
        System.out.println(CYAN + BOLD + "  ╚══════════════════════════════════════════════════╝" + RESET);
        System.out.println();
    }

    // ─── Broadcast ──────────────────────────────────────────────────
    public static synchronized void broadcast(String message, String senderName) {
        log(MAGENTA, "BROADCAST", message);
        ChatLogger.log(message);
        for (Map.Entry<String, PrintWriter> entry : activeClients.entrySet()) {
            if (!entry.getKey().equals(senderName)) {
                entry.getValue().println(message);
            }
        }
    }

    // ─── Private Message ────────────────────────────────────────────
    public static synchronized void privateMessage(String from, String to, String message) {
        PrintWriter targetWriter = activeClients.get(to);
        if (targetWriter != null) {
            targetWriter.println("[Private from " + from + "]: " + message);
            ChatLogger.log("PRIVATE | " + from + " -> " + to + ": " + message);
            log(YELLOW, "PM", from + " -> " + to + ": " + message);
        } else {
            PrintWriter senderWriter = activeClients.get(from);
            if (senderWriter != null) {
                senderWriter.println(">> User '" + to + "' online nahi hai.");
            }
        }
    }

    // ─── User List ──────────────────────────────────────────────────
    public static synchronized void sendUserList(String requester) {
        PrintWriter pw = activeClients.get(requester);
        if (pw != null) {
            pw.println("\n+----- Online Users (" + activeClients.size() + ") -----+");
            for (String name : activeClients.keySet()) {
                pw.println("|  * " + name + (name.equals(requester) ? " (You)" : ""));
            }
            pw.println("+--------------------------------+\n");
        }
    }

    public static synchronized void addClient(String username, PrintWriter writer) {
        activeClients.put(username, writer);
        log(GREEN, "JOIN", username + " connected. Total: " + activeClients.size());
    }

    public static synchronized void removeClient(String username) {
        activeClients.remove(username);
        log(RED, "LEFT", username + " disconnected. Total: " + activeClients.size());
    }

    public static synchronized boolean usernameExists(String username) {
        return activeClients.containsKey(username);
    }

    // ─── Client Handler ─────────────────────────────────────────────
    static class ClientHandler implements Runnable {

        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Send welcome banner to client
                out.println(" ");
                out.println("  +==================================================+");
                out.println("  |          JavaChat Server - Connected!            |");
                out.println("  |          Port: 5000  |  Core Java Chat           |");
                out.println("  +==================================================+");
                out.println("  Commands: /list | /pm <user> <msg> | /quit");
                out.println("  +--------------------------------------------------+");
                out.println(" ");
                out.println(">> Apna username enter karo:");

                // Username validation
                while (true) {
                    username = in.readLine();
                    if (username == null || username.trim().isEmpty()) {
                        out.println(">> [!] Username empty nahi ho sakta. Dobara try karo:");
                        continue;
                    }
                    username = username.trim();
                    if (ChatServer.usernameExists(username)) {
                        out.println(">> [!] '" + username + "' pehle se liya hua hai. Dusra naam lo:");
                    } else {
                        break;
                    }
                }

                ChatServer.addClient(username, out);
                out.println("\n>> [+] Welcome, " + username + "! Tum chat room mein aa gaye ho.\n");
                ChatServer.broadcast("[+] " + username + " join ho gaya!", username);
                ChatLogger.log(username + " joined.");

                // Message loop
                String message;
                while ((message = in.readLine()) != null) {
                    message = message.trim();
                    if (message.equalsIgnoreCase("/quit")) {
                        break;
                    } else if (message.equalsIgnoreCase("/list")) {
                        ChatServer.sendUserList(username);
                    } else if (message.equalsIgnoreCase("/history")) {
                        ChatLogger.printHistory();
                        out.println(">> History server console pe print ho gayi.");
                    } else if (message.startsWith("/pm ")) {
                        String[] parts = message.split(" ", 3);
                        if (parts.length < 3) {
                            out.println(">> [!] Format: /pm <username> <message>");
                        } else {
                            ChatServer.privateMessage(username, parts[1], parts[2]);
                        }
                    } else {
                        String formatted = username + " >> " + message;
                        out.println(formatted);
                        ChatServer.broadcast(formatted, username);
                    }
                }

            } catch (IOException e) {
                ChatServer.log(RED, "WARN", "Abrupt disconnect: " + username);
            } finally {
                if (username != null) {
                    ChatServer.removeClient(username);
                    ChatServer.broadcast("[-] " + username + " chala gaya.", username);
                    ChatLogger.log(username + " left.");
                }
                try { socket.close(); } catch (IOException ignored) {}
            }
        }
    }
}