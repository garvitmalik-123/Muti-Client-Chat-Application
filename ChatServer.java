
package project_2;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    private static final int PORT = 5000;
    private static final Map<String, PrintWriter> activeClients = new HashMap<>();
    private static ServerGUI gui;

    public static void setGUI(ServerGUI g) {
        gui = g;
    }

    public static void start() throws IOException {
        // FIX 1: try-with-resources so serverSocket always closes cleanly
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log("SERVER", "Started on port " + PORT);

            try {
                InetAddress localIP = InetAddress.getLocalHost();
                log("HOST IP", localIP.getHostAddress() + "  ← Share this IP with clients");
            } catch (UnknownHostException e) {
                log("WARN", "Could not detect LAN IP.");
            }

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        }
    }

    public static synchronized void broadcast(String message, String senderName) {
        log("BROADCAST", message);
        ChatLogger.log(message);
        for (Map.Entry<String, PrintWriter> entry : activeClients.entrySet()) {
            if (!entry.getKey().equals(senderName)) {
                entry.getValue().println(message);
            }
        }
        if (gui != null) gui.appendMessage(message, "broadcast");
    }

    public static synchronized void privateMessage(String from, String to, String message) {
        PrintWriter targetWriter = activeClients.get(to);
        if (targetWriter != null) {
            String pm = "[Private from " + from + "]: " + message;
            targetWriter.println(pm);
            ChatLogger.log("PRIVATE | " + from + " -> " + to + ": " + message);
            log("PM", from + " -> " + to + ": " + message);
            if (gui != null) gui.appendMessage("PM: " + from + " → " + to + ": " + message, "private");
        } else {
            PrintWriter senderWriter = activeClients.get(from);
            if (senderWriter != null) {
                senderWriter.println(">> User '" + to + "' is not online.");
            }
        }
    }

    public static synchronized void sendUserList(String requester) {
        PrintWriter pw = activeClients.get(requester);
        if (pw != null) {
            pw.println("USERLIST:" + String.join(",", activeClients.keySet()));
        }
    }

    public static synchronized void addClient(String username, PrintWriter writer) {
        activeClients.put(username, writer);
        log("JOIN", username + " connected. Total: " + activeClients.size());
        if (gui != null) {
            gui.appendMessage(username + " has joined the chat.", "join");
            gui.updateUserList(new ArrayList<>(activeClients.keySet()));
        }
    }

    public static synchronized void removeClient(String username) {
        activeClients.remove(username);
        log("LEFT", username + " disconnected. Total: " + activeClients.size());
        if (gui != null) {
            gui.updateUserList(new ArrayList<>(activeClients.keySet()));
        }
    }

    public static synchronized boolean usernameExists(String username) {
        return activeClients.containsKey(username);
    }

    public static synchronized List<String> getOnlineUsers() {
        return new ArrayList<>(activeClients.keySet());
    }

    // FIX 2: snapshot of activeClients for safe broadcast after removal
    private static synchronized Map<String, PrintWriter> getClientSnapshot() {
        return new HashMap<>(activeClients);
    }

    private static void log(String tag, String msg) {
        String line = "[" + tag + "] " + msg;
        System.out.println(line);
        if (gui != null) gui.appendLog(line);
    }

    // ─── Client Handler ─────────────────────────────────────────────
    static class ClientHandler implements Runnable {
        private final Socket socket;
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

                out.println("WELCOME:JavaChat Server Connected! Port 5000");
                out.println(">> Enter your username:");

                // Username negotiation loop
                while (true) {
                    username = in.readLine();
                    if (username == null) return; // client disconnected before sending name
                    username = username.trim();
                    if (username.isEmpty()) {
                        out.println(">> [!] Username cannot be empty. Please try again:");
                        continue;
                    }
                    if (ChatServer.usernameExists(username)) {
                        out.println(">> [!] '" + username + "' is already taken. Please choose another name:");
                        out.println(">> Enter your username:");
                    } else {
                        break;
                    }
                }

                ChatServer.addClient(username, out);
                out.println(">> [+] Welcome, " + username + "! You have joined the chat room.");
                ChatServer.broadcast("[+] " + username + " has joined!", username);
                ChatLogger.log(username + " joined.");

                // Send current user list to ALL clients
                broadcastUserList();

                String message;
                while ((message = in.readLine()) != null) {
                    message = message.trim();
                    if (message.equalsIgnoreCase("/quit")) {
                        break;
                    } else if (message.equalsIgnoreCase("/list")) {
                        ChatServer.sendUserList(username);
                    } else if (message.startsWith("/pm ")) {
                        String[] parts = message.split(" ", 3);
                        if (parts.length < 3) {
                            out.println(">> [!] Format: /pm <username> <message>");
                        } else {
                            ChatServer.privateMessage(username, parts[1], parts[2]);
                        }
                    } else {
                        String formatted = username + " >> " + message;
                        out.println(formatted);          // echo back to sender
                        ChatServer.broadcast(formatted, username);
                    }
                }

            } catch (IOException e) {
                System.out.println("[WARN] Abrupt disconnect: " + username);
            } finally {
                if (username != null) {
                    // FIX 3: removeClient FIRST, then broadcast using snapshot
                    // so we never iterate a map that's being modified
                    ChatServer.removeClient(username);
                    ChatServer.broadcast("[-] " + username + " has left.", username);
                    ChatLogger.log(username + " left.");

                    // Safe userlist broadcast using snapshot
                    Map<String, PrintWriter> snapshot = getClientSnapshot();
                    String userList = "USERLIST:" + String.join(",", snapshot.keySet());
                    for (PrintWriter pw : snapshot.values()) {
                        pw.println(userList);
                    }
                }
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private void broadcastUserList() {
            Map<String, PrintWriter> snapshot = getClientSnapshot();
            String userList = "USERLIST:" + String.join(",", snapshot.keySet());
            for (PrintWriter pw : snapshot.values()) {
                pw.println(userList);
            }
        }
    }
}