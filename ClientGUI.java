package project_2;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClientGUI extends JFrame {

    // ─── Colors ─────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(18, 18, 28);
    private static final Color BG_PANEL     = new Color(26, 26, 40);
    private static final Color BG_INPUT     = new Color(35, 35, 52);
    private static final Color ACCENT_CYAN  = new Color(0, 200, 220);
    private static final Color ACCENT_GREEN = new Color(0, 210, 120);
    private static final Color ACCENT_RED   = new Color(220, 60, 60);
    private static final Color ACCENT_YELLOW= new Color(255, 200, 50);
    private static final Color ACCENT_PURPLE= new Color(160, 80, 220);
    private static final Color MY_MSG_COLOR = new Color(0, 180, 200);
    private static final Color OTHER_MSG    = new Color(200, 200, 220);
    private static final Color TEXT_GRAY    = new Color(130, 130, 160);
    private static final Color BG_MSG_ME    = new Color(0, 60, 80);
    private static final Color BG_MSG_OTHER = new Color(35, 35, 55);

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    // ─── Network ────────────────────────────────────────────────────
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private volatile boolean connected = false;
    // FIX: track whether username has been sent to server
    private volatile boolean usernameSent = false;

    // ─── Components ─────────────────────────────────────────────────
    private JTextPane chatPane;
    private StyledDocument chatDoc;
    private JTextField inputField;
    private JButton sendBtn;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private JLabel statusLabel;
    private JLabel serverLabel;
    private JComboBox<String> pmTargetBox;
    private JCheckBox pmCheckBox;

    // ─── Connect Panel Components ────────────────────────────────────
    private JTextField ipField;
    private JTextField usernameField;

    public ClientGUI() {
        setTitle("JavaChat — Client");
        setSize(950, 660);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(750, 520));

        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));

        // ── Top Bar ──────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_PANEL);
        topBar.setBorder(new EmptyBorder(10, 18, 10, 18));

        JLabel title = new JLabel("  ◈  JavaChat Client");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(160, 80, 220));

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        topRight.setBackground(BG_PANEL);

        serverLabel = new JLabel("Not connected");
        serverLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        serverLabel.setForeground(TEXT_GRAY);

        statusLabel = new JLabel("● OFFLINE");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setForeground(ACCENT_RED);

        JButton disconnectBtn = makeButton("Disconnect", ACCENT_RED);
        disconnectBtn.addActionListener(e -> disconnect());

        topRight.add(serverLabel);
        topRight.add(statusLabel);
        topRight.add(disconnectBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── Connect Panel (shown first) ──────────────────────────────
        JPanel connectPanel = buildConnectPanel();
        add(connectPanel, BorderLayout.CENTER);
    }

    private JPanel buildConnectPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG_DARK);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 100), 2),
                new EmptyBorder(30, 40, 30, 40)));

        JLabel connectTitle = new JLabel("Connect to Server");
        connectTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        connectTitle.setForeground(ACCENT_CYAN);
        connectTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Enter server IP and your username");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        String myIP = "unknown";
        try { myIP = InetAddress.getLocalHost().getHostAddress(); } catch (Exception ignored) {}
        JLabel myIPLabel = new JLabel("Your IP: " + myIP);
        myIPLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        myIPLabel.setForeground(ACCENT_GREEN);
        myIPLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        ipField = makeTextField("Server IP  (e.g. 192.168.1.5)", 22);
        usernameField = makeTextField("Username", 22);

        JButton connectBtn = makeButton("  Connect  ", ACCENT_GREEN);
        connectBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        connectBtn.addActionListener(e -> attemptConnect());

        ActionListener connectAction = e -> attemptConnect();
        ipField.addActionListener(connectAction);
        usernameField.addActionListener(connectAction);

        card.add(connectTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(4));
        card.add(myIPLabel);
        card.add(Box.createVerticalStrut(24));
        card.add(makeFieldLabel("Server IP Address"));
        card.add(Box.createVerticalStrut(5));
        card.add(ipField);
        card.add(Box.createVerticalStrut(14));
        card.add(makeFieldLabel("Your Username"));
        card.add(Box.createVerticalStrut(5));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(24));
        card.add(connectBtn);

        wrapper.add(card);
        return wrapper;
    }

    private void attemptConnect() {
        String ip = ipField.getText().trim();
        String uname = usernameField.getText().trim();

        if (ip.isEmpty()) ip = "localhost";

        if (uname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final String finalIp = ip;
        final String finalUname = uname;

        new Thread(() -> {
            try {
                socket = new Socket(finalIp, 5000);
                out = new PrintWriter(socket.getOutputStream(), true);
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                username = finalUname;
                connected = true;
                usernameSent = false; // reset for fresh connection

                SwingUtilities.invokeLater(() -> {
                    serverLabel.setText("Connected: " + finalIp + ":5000");
                    statusLabel.setText("● ONLINE");
                    statusLabel.setForeground(ACCENT_GREEN);
                    switchToChatUI();
                });

                // FIX: Robust handshake — respond to any username prompt,
                // handle taken-name retries, then process chat messages
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("WELCOME:")) {
                        // skip banner, wait for username prompt

                    } else if (line.contains("Enter your username:")) {
                        // Server is asking for name — send it
                        out.println(username);
                        usernameSent = true;

                    } else if (line.contains("is already taken") || line.contains("cannot be empty")) {
                        // Username rejected — ask user to pick a new one
                        final String serverMsg = line;
                        SwingUtilities.invokeLater(() -> {
                            String newName = JOptionPane.showInputDialog(
                                    ClientGUI.this,
                                    serverMsg + "\nEnter a different username:",
                                    "Username Taken",
                                    JOptionPane.WARNING_MESSAGE);
                            if (newName != null && !newName.trim().isEmpty()) {
                                username = newName.trim();
                                setTitle("JavaChat — " + username);
                            }
                        });
                        // Wait for EDT to update username, then send
                        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                        out.println(username);

                    } else if (line.startsWith("USERLIST:")) {
                        updateUserList(line.substring(9));

                    } else {
                        // Regular chat message
                        final String msg = line;
                        SwingUtilities.invokeLater(() -> displayMessage(msg));
                    }
                }

            } catch (ConnectException e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this,
                                "Could not connect to server at " + finalIp + ":5000\n" +
                                        "Make sure the server is running.",
                                "Connection Failed", JOptionPane.ERROR_MESSAGE));
            } catch (IOException e) {
                if (connected) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("● DISCONNECTED");
                        statusLabel.setForeground(ACCENT_RED);
                        if (chatPane != null) displaySystemMessage("Connection to server lost.");
                    });
                }
            }
        }).start();
    }

    private void switchToChatUI() {
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout(0, 0));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_PANEL);
        topBar.setBorder(new EmptyBorder(10, 18, 10, 18));

        JLabel title = new JLabel("  ◈  JavaChat  —  " + username);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ACCENT_PURPLE);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        topRight.setBackground(BG_PANEL);
        topRight.add(serverLabel);
        topRight.add(statusLabel);
        JButton discBtn = makeButton("Disconnect", ACCENT_RED);
        discBtn.addActionListener(e -> disconnect());
        topRight.add(discBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);
        getContentPane().add(topBar, BorderLayout.NORTH);

        // ── Main Split ───────────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(670);
        split.setDividerSize(3);
        split.setBorder(null);

        // Chat pane
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setBackground(BG_DARK);
        chatPane.setBorder(new EmptyBorder(10, 14, 10, 14));
        chatDoc = chatPane.getStyledDocument();

        JScrollPane chatScroll = new JScrollPane(chatPane);
        chatScroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(50,50,75)));
        chatScroll.getViewport().setBackground(BG_DARK);
        chatScroll.getVerticalScrollBar().setBackground(BG_PANEL);

        // Right: Users panel
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setBackground(BG_DARK);
        rightPanel.setBorder(new EmptyBorder(0, 4, 0, 0));

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBackground(BG_PANEL);
        userList.setForeground(ACCENT_GREEN);
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userList.setCellRenderer(new UserListRenderer());
        userList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selected = userList.getSelectedValue();
                    if (selected != null && !selected.equals(username)) {
                        pmCheckBox.setSelected(true);
                        pmTargetBox.setSelectedItem(selected);
                        inputField.requestFocus();
                    }
                }
            }
        });

        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(50,50,75), 1),
                "  Online Users", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), TEXT_GRAY));
        userScroll.setBackground(BG_PANEL);
        userScroll.getViewport().setBackground(BG_PANEL);

        JTextArea helpArea = new JTextArea(
                "  Commands:\n" +
                        "  Double-click user → PM\n" +
                        "  /list  → Online users\n" +
                        "  /quit  → Leave chat\n");
        helpArea.setEditable(false);
        helpArea.setBackground(new Color(20, 20, 32));
        helpArea.setForeground(TEXT_GRAY);
        helpArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        helpArea.setBorder(new EmptyBorder(8, 6, 8, 6));

        rightPanel.add(userScroll, BorderLayout.CENTER);
        rightPanel.add(helpArea, BorderLayout.SOUTH);

        split.setLeftComponent(chatScroll);
        split.setRightComponent(rightPanel);
        getContentPane().add(split, BorderLayout.CENTER);

        // ── Bottom Input Bar ─────────────────────────────────────────
        JPanel bottomBar = new JPanel(new BorderLayout(8, 0));
        bottomBar.setBackground(BG_PANEL);
        bottomBar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(50, 50, 75)),
                new EmptyBorder(10, 14, 10, 14)));

        JPanel pmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pmPanel.setBackground(BG_PANEL);

        pmCheckBox = new JCheckBox("Private to:");
        pmCheckBox.setBackground(BG_PANEL);
        pmCheckBox.setForeground(ACCENT_YELLOW);
        pmCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        pmTargetBox = new JComboBox<>();
        pmTargetBox.setBackground(BG_INPUT);
        pmTargetBox.setForeground(TEXT_GRAY);
        pmTargetBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pmTargetBox.setPreferredSize(new Dimension(120, 28));

        pmPanel.add(pmCheckBox);
        pmPanel.add(pmTargetBox);

        inputField = new JTextField();
        inputField.setBackground(BG_INPUT);
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(ACCENT_CYAN);
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 100), 1),
                new EmptyBorder(6, 12, 6, 12)));
        inputField.addActionListener(e -> sendMessage());

        sendBtn = makeButton("Send  ➤", ACCENT_CYAN);
        sendBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sendBtn.addActionListener(e -> sendMessage());

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setBackground(BG_PANEL);
        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(sendBtn, BorderLayout.EAST);

        bottomBar.add(pmPanel, BorderLayout.NORTH);
        bottomBar.add(inputRow, BorderLayout.CENTER);
        getContentPane().add(bottomBar, BorderLayout.SOUTH);

        revalidate();
        repaint();

        displaySystemMessage("Connected as " + username + ". Welcome to JavaChat!");
        inputField.requestFocus();
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty() || !connected) return;

        String toSend;
        if (pmCheckBox.isSelected() && pmTargetBox.getSelectedItem() != null) {
            String target = pmTargetBox.getSelectedItem().toString();
            toSend = "/pm " + target + " " + text;
            displayOwnMessage("[Private to " + target + "]: " + text, true);
        } else {
            toSend = text;
            displayOwnMessage(text, false);
        }

        out.println(toSend);
        inputField.setText("");

        if (text.equalsIgnoreCase("/quit")) {
            disconnect();
        }
    }

    private void displayMessage(String msg) {
        if (msg == null || msg.isEmpty()) return;

        try {
            String time = "[" + LocalTime.now().format(TIME_FMT) + "] ";

            if (msg.startsWith("[+]")) {
                appendStyled(time + msg + "\n", ACCENT_GREEN, false, 12, BG_DARK);
            } else if (msg.startsWith("[-]")) {
                appendStyled(time + msg + "\n", ACCENT_RED, false, 12, BG_DARK);
            } else if (msg.startsWith("[Private from")) {
                appendStyled(time, TEXT_GRAY, false, 11, BG_DARK);
                appendStyled(msg + "\n", ACCENT_PURPLE, true, 13, new Color(40, 20, 60));
            } else if (msg.startsWith(">> [+]") || msg.startsWith(">> ")) {
                appendStyled(time + msg + "\n", ACCENT_YELLOW, false, 12, BG_DARK);
            } else if (msg.contains(" >> ")) {
                int idx = msg.indexOf(" >> ");
                String user = msg.substring(0, idx);
                String text = msg.substring(idx + 4);
                appendStyled(time, TEXT_GRAY, false, 11, BG_DARK);
                appendStyled(user + ": ", ACCENT_CYAN, true, 13, BG_MSG_OTHER);
                appendStyled(text + "\n", OTHER_MSG, false, 13, BG_MSG_OTHER);
            } else if (msg.startsWith("USERLIST:")) {
                updateUserList(msg.substring(9));
            } else {
                appendStyled(time + msg + "\n", TEXT_GRAY, false, 12, BG_DARK);
            }

            chatPane.setCaretPosition(chatDoc.getLength());
        } catch (Exception ignored) {}
    }

    private void displayOwnMessage(String text, boolean isPrivate) {
        try {
            String time = "[" + LocalTime.now().format(TIME_FMT) + "] ";
            appendStyled(time, TEXT_GRAY, false, 11, BG_DARK);
            appendStyled("You: ", MY_MSG_COLOR, true, 13, BG_MSG_ME);
            appendStyled(text + "\n", Color.WHITE, false, 13, BG_MSG_ME);
            chatPane.setCaretPosition(chatDoc.getLength());
        } catch (Exception ignored) {}
    }

    private void displaySystemMessage(String msg) {
        try {
            appendStyled("─── " + msg + " ───\n", TEXT_GRAY, false, 11, BG_DARK);
            chatPane.setCaretPosition(chatDoc.getLength());
        } catch (Exception ignored) {}
    }

    private void appendStyled(String text, Color fg, boolean bold, int size, Color bg)
            throws BadLocationException {
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, fg);
        StyleConstants.setBold(attr, bold);
        StyleConstants.setFontFamily(attr, "Segoe UI");
        StyleConstants.setFontSize(attr, size);
        StyleConstants.setBackground(attr, bg);
        chatDoc.insertString(chatDoc.getLength(), text, attr);
    }

    private void updateUserList(String csv) {
        SwingUtilities.invokeLater(() -> {
            if (userListModel == null || pmTargetBox == null) return;
            userListModel.clear();
            pmTargetBox.removeAllItems();
            if (csv.isEmpty()) return;
            for (String u : csv.split(",")) {
                userListModel.addElement(u);
                if (!u.equals(username)) pmTargetBox.addItem(u);
            }
        });
    }

    private void disconnect() {
        connected = false;
        if (out != null) out.println("/quit");
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        statusLabel.setText("● OFFLINE");
        statusLabel.setForeground(ACCENT_RED);
        if (chatPane != null) displaySystemMessage("You have disconnected.");
    }

    // ─── Helpers ────────────────────────────────────────────────────
    private JTextField makeTextField(String placeholder, int cols) {
        JTextField field = new JTextField(cols) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g.setColor(TEXT_GRAY);
                    g.setFont(getFont().deriveFont(Font.ITALIC));
                    g.drawString(placeholder, 12, getHeight() / 2 + 5);
                }
            }
        };
        field.setBackground(BG_INPUT);
        field.setForeground(Color.WHITE);
        field.setCaretColor(ACCENT_CYAN);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 100), 1),
                new EmptyBorder(8, 12, 8, 12)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        return field;
    }

    private JLabel makeFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_GRAY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton makeButton(String text, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setBackground(BG_INPUT);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg.darker(), 1),
                new EmptyBorder(6, 14, 6, 14)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(50,50,75)); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(BG_INPUT); }
        });
        return btn;
    }

    // ─── User List Renderer ─────────────────────────────────────────
    private class UserListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            boolean isMe = value.toString().equals(username);
            label.setText("  " + (isMe ? "★ " : "● ") + value + (isMe ? "  (you)" : ""));
            label.setFont(new Font("Segoe UI", isMe ? Font.BOLD : Font.PLAIN, 13));
            label.setForeground(isSelected ? Color.WHITE : (isMe ? ACCENT_YELLOW : ACCENT_GREEN));
            label.setBackground(isSelected ? new Color(0, 100, 70) : BG_PANEL);
            label.setBorder(new EmptyBorder(5, 6, 5, 6));
            return label;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}