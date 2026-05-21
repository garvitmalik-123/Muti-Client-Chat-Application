package chatapp;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;
    private static volatile boolean running = true;

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
    // ────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        printClientBanner();

        System.out.println(CYAN + "  Connecting to server..." + RESET);
        System.out.println();

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {

            System.out.println(GREEN + BOLD + "  [✔] Server se Connected!" + RESET);
            System.out.println(YELLOW + "  ──────────────────────────────────────────────────" + RESET);
            System.out.println();

            BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out    = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner    = new Scanner(System.in);

            // Thread: Server se messages receive karo
            Thread receiveThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while (running && (serverMessage = in.readLine()) != null) {
                        printColoredMessage(serverMessage);
                    }
                } catch (IOException e) {
                    if (running) {
                        System.out.println(RED + "\n  [✘] Server se connection toot gaya." + RESET);
                    }
                } finally {
                    running = false;
                }
            });

            receiveThread.setDaemon(true);
            receiveThread.start();

            // Main thread: Messages send karo
            while (running) {
                if (scanner.hasNextLine()) {
                    String userInput = scanner.nextLine().trim();
                    if (!userInput.isEmpty()) {
                        out.println(userInput);
                        if (userInput.equalsIgnoreCase("/quit")) {
                            System.out.println(YELLOW + "\n  Goodbye! Phir milenge. 👋" + RESET);
                            running = false;
                            break;
                        }
                    }
                }
            }

        } catch (ConnectException e) {
            System.out.println(RED + BOLD + "\n  [✘] Server nahi mila!" + RESET);
            System.out.println(YELLOW + "  Pehle ChatServer.java run karo, phir yeh chalao." + RESET);
        } catch (IOException e) {
            System.out.println(RED + "  [✘] Error: " + e.getMessage() + RESET);
        }

        System.out.println();
        System.out.println(CYAN + "  ══════════════════════════════════════════════════" + RESET);
        System.out.println(CYAN + "  Connection closed. Program band ho raha hai..." + RESET);
        System.out.println(CYAN + "  ══════════════════════════════════════════════════" + RESET);
    }

    // ─── Color logic based on message content ───────────────────────
    static void printColoredMessage(String msg) {
        if (msg.contains("[+]")) {
            // Join message → Green
            System.out.println(GREEN + "  " + msg + RESET);
        } else if (msg.contains("[-]")) {
            // Leave message → Red
            System.out.println(RED + "  " + msg + RESET);
        } else if (msg.contains("[Private")) {
            // Private message → Magenta
            System.out.println(MAGENTA + BOLD + "  " + msg + RESET);
        } else if (msg.contains("[!]") || msg.contains(">>")) {
            // Server notice → Yellow
            System.out.println(YELLOW + "  " + msg + RESET);
        } else if (msg.startsWith("  +") || msg.startsWith("  |") || msg.startsWith("  +--")) {
            // Borders/boxes → Cyan
            System.out.println(CYAN + msg + RESET);
        } else if (msg.contains(" >> ")) {
            // Chat message → White with bold username
            int idx = msg.indexOf(" >> ");
            String user = msg.substring(0, idx);
            String text = msg.substring(idx);
            System.out.println("  " + CYAN + BOLD + user + RESET + WHITE + text + RESET);
        } else {
            System.out.println("  " + WHITE + msg + RESET);
        }
    }

    // ─── Client Banner ──────────────────────────────────────────────
    static void printClientBanner() {
        System.out.println();
        System.out.println(MAGENTA + BOLD + "  ╔══════════════════════════════════════════════════╗" + RESET);
        System.out.println(MAGENTA + BOLD + "  ║                                                  ║" + RESET);
        System.out.println(MAGENTA + BOLD + "  ║" + CYAN + BOLD   + "        ██╗ █████╗ ██╗   ██╗ █████╗             " + MAGENTA + "║" + RESET);
        System.out.println(MAGENTA + BOLD + "  ║" + CYAN + BOLD   + "        ██║██╔══██╗██║   ██║██╔══██╗            " + MAGENTA + "║" + RESET);
        System.out.println(MAGENTA + BOLD + "  ║" + CYAN + BOLD   + "        ██║███████║██║   ██║███████║            " + MAGENTA + "║" + RESET);
        System.out.println(MAGENTA + BOLD + "  ║" + CYAN + BOLD   + "   ██   ██║██╔══██║╚██╗ ██╔╝██╔══██║           " + MAGENTA + "║" + RESET);
        System.out.println(MAGENTA + BOLD + "  ║" + CYAN + BOLD   + "   ╚█████╔╝██║  ██║ ╚████╔╝ ██║  ██║           " + MAGENTA + "║" + RESET);
        System.out.println(MAGENTA + BOLD + "  ║" + CYAN + BOLD   + "    ╚════╝ ╚═╝  ╚═╝  ╚═══╝  ╚═╝  ╚═╝           " + MAGENTA + "║" + RESET);
        System.out.println(MAGENTA + BOLD + "  ║" + YELLOW        + "              C H A T   C L I E N T            " + MAGENTA + "║" + RESET);
        System.out.println(MAGENTA + BOLD + "  ║                                                  ║" + RESET);
        System.out.println(MAGENTA + BOLD + "  ╠══════════════════════════════════════════════════╣" + RESET);
        System.out.println(MAGENTA + BOLD + "  ║  " + GREEN + "/list" + WHITE + "              → Online users dekho      " + MAGENTA + BOLD + "║" + RESET);
        System.out.println(MAGENTA + BOLD + "  ║  " + GREEN + "/pm <user> <msg>" + WHITE + "   → Private message bhejo   " + MAGENTA + BOLD + "║" + RESET);
        System.out.println(MAGENTA + BOLD + "  ║  " + GREEN + "/history" + WHITE + "           → Chat history dekho     " + MAGENTA + BOLD + "║" + RESET);
        System.out.println(MAGENTA + BOLD + "  ║  " + RED   + "/quit" + WHITE + "              → Chat chodo             " + MAGENTA + BOLD + "║" + RESET);
        System.out.println(MAGENTA + BOLD + "  ╚══════════════════════════════════════════════════╝" + RESET);
        System.out.println();
    }
}