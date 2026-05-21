package project_2;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.List;

public class ServerGUI extends JFrame {

    // ─── Colors ─────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(18, 18, 28);
    private static final Color BG_PANEL     = new Color(26, 26, 40);
    private static final Color BG_INPUT     = new Color(35, 35, 52);
    private static final Color ACCENT_CYAN  = new Color(0, 200, 220);
    private static final Color ACCENT_GREEN = new Color(0, 210, 120);
    private static final Color ACCENT_RED   = new Color(220, 60, 60);
    private static final Color ACCENT_YELLOW= new Color(255, 200, 50);
    private static final Color ACCENT_PURPLE= new Color(160, 80, 220);
    private static final Color TEXT_WHITE   = new Color(220, 220, 235);
    private static final Color TEXT_GRAY    = new Color(130, 130, 160);

    // ─── Components ─────────────────────────────────────────────────
    private JTextPane chatPane;
    private JTextPane logPane;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private JLabel statusLabel;
    private JLabel ipLabel;
    private JLabel clientCountLabel;
    private StyledDocument chatDoc;
    private StyledDocument logDoc;

    public ServerGUI() {
        setTitle("JavaChat — Server");
        setSize(1000, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 550));

        buildUI();
        showLanIP();
        setVisible(true);
    }

    private void buildUI() {
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));

        // ── Top Title Bar ────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_PANEL);
        topBar.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("  ◈  JavaChat Server");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ACCENT_CYAN);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        topRight.setBackground(BG_PANEL);

        ipLabel = new JLabel("IP: detecting...");
        ipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ipLabel.setForeground(ACCENT_GREEN);

        clientCountLabel = new JLabel("Clients: 0");
        clientCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        clientCountLabel.setForeground(ACCENT_YELLOW);

        statusLabel = new JLabel("● OFFLINE");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setForeground(ACCENT_RED);

        topRight.add(ipLabel);
        topRight.add(Box.createHorizontalStrut(10));
        topRight.add(clientCountLabel);
        topRight.add(Box.createHorizontalStrut(10));
        topRight.add(statusLabel);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ── Center: Chat + Users ─────────────────────────────────────
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplit.setDividerLocation(680);
        centerSplit.setDividerSize(3);
        centerSplit.setBorder(null);
        centerSplit.setBackground(BG_DARK);

        // Chat area
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setBackground(BG_DARK);
        chatPane.setBorder(new EmptyBorder(10, 14, 10, 14));
        chatDoc = chatPane.getStyledDocument();

        JScrollPane chatScroll = new JScrollPane(chatPane);
        chatScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(50,50,75), 1),
                "  Live Chat Monitor", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), TEXT_GRAY));
        chatScroll.setBackground(BG_DARK);
        chatScroll.getViewport().setBackground(BG_DARK);
        chatScroll.getVerticalScrollBar().setBackground(BG_PANEL);

        // Right panel: Users + Log
        JPanel rightPanel = new JPanel(new BorderLayout(0, 6));
        rightPanel.setBackground(BG_DARK);
        rightPanel.setBorder(new EmptyBorder(0, 3, 0, 0));

        // Online Users list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBackground(BG_PANEL);
        userList.setForeground(ACCENT_GREEN);
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userList.setSelectionBackground(new Color(0, 120, 80));
        userList.setSelectionForeground(Color.WHITE);
        userList.setCellRenderer(new UserListRenderer());

        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(200, 200));
        userScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(50,50,75), 1),
                "  Online Users", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), TEXT_GRAY));
        userScroll.setBackground(BG_PANEL);
        userScroll.getViewport().setBackground(BG_PANEL);

        // Server log
        logPane = new JTextPane();
        logPane.setEditable(false);
        logPane.setBackground(new Color(15, 15, 22));
        logPane.setBorder(new EmptyBorder(8, 10, 8, 10));
        logDoc = logPane.getStyledDocument();

        JScrollPane logScroll = new JScrollPane(logPane);
        logScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(50,50,75), 1),
                "  Server Log", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), TEXT_GRAY));
        logScroll.setBackground(BG_PANEL);
        logScroll.getViewport().setBackground(new Color(15, 15, 22));

        rightPanel.add(userScroll, BorderLayout.NORTH);
        rightPanel.add(logScroll, BorderLayout.CENTER);

        centerSplit.setLeftComponent(chatScroll);
        centerSplit.setRightComponent(rightPanel);
        add(centerSplit, BorderLayout.CENTER);

        // ── Bottom Bar ───────────────────────────────────────────────
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        bottomBar.setBackground(BG_PANEL);
        bottomBar.setBorder(new MatteBorder(1, 0, 0, 0, new Color(50, 50, 75)));

        JButton startBtn = makeButton("▶  Start Server", ACCENT_GREEN);
        JButton historyBtn = makeButton("📋  View History", ACCENT_CYAN);
        JButton clearBtn = makeButton("🗑  Clear History", ACCENT_RED);

        startBtn.addActionListener(e -> startServer(startBtn));
        historyBtn.addActionListener(e -> showHistory());
        clearBtn.addActionListener(e -> clearHistory());

        bottomBar.add(startBtn);
        bottomBar.add(historyBtn);
        bottomBar.add(clearBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void showLanIP() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            ipLabel.setText("Your IP: " + ip.getHostAddress());
        } catch (UnknownHostException e) {
            ipLabel.setText("IP: unknown");
        }
    }

    private void startServer(JButton btn) {
        btn.setEnabled(false);
        btn.setText("● Running...");
        btn.setBackground(new Color(30, 80, 50));
        statusLabel.setText("● ONLINE");
        statusLabel.setForeground(ACCENT_GREEN);

        new Thread(() -> {
            try {
                ChatServer.setGUI(this);
                ChatServer.start();
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("● ERROR");
                    statusLabel.setForeground(ACCENT_RED);
                    appendLog("[ERROR] " + ex.getMessage());
                });
            }
        }).start();
    }

    private void showHistory() {
        String history = ChatLogger.getHistory();
        JTextArea area = new JTextArea(history);
        area.setEditable(false);
        area.setBackground(BG_DARK);
        area.setForeground(TEXT_WHITE);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(600, 400));
        sp.getViewport().setBackground(BG_DARK);

        JOptionPane.showMessageDialog(this, sp, "Chat History",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void clearHistory() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear all chat history?",
                "Clear History", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ChatLogger.clearHistory();
            appendLog("[INFO] Chat history cleared.");
        }
    }

    // ─── Called from ChatServer ──────────────────────────────────────
    public void appendMessage(String msg, String type) {
        SwingUtilities.invokeLater(() -> {
            try {
                Color color;
                switch (type) {
                    case "join":    color = ACCENT_GREEN;  break;
                    case "leave":   color = ACCENT_RED;    break;
                    case "private": color = ACCENT_PURPLE; break;
                    default:        color = TEXT_WHITE;    break;
                }

                SimpleAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setForeground(attr, color);
                StyleConstants.setFontFamily(attr, "Segoe UI");
                StyleConstants.setFontSize(attr, 13);

                chatDoc.insertString(chatDoc.getLength(), msg + "\n", attr);
                chatPane.setCaretPosition(chatDoc.getLength());
            } catch (BadLocationException ignored) {}
        });
    }

    public void appendLog(String line) {
        SwingUtilities.invokeLater(() -> {
            try {
                Color color = TEXT_GRAY;
                if (line.contains("[JOIN]") || line.contains("[READY]")) color = ACCENT_GREEN;
                else if (line.contains("[LEFT]") || line.contains("[ERROR]")) color = ACCENT_RED;
                else if (line.contains("[PM]")) color = ACCENT_PURPLE;
                else if (line.contains("[BROADCAST]")) color = TEXT_WHITE;
                else if (line.contains("[HOST IP]")) color = ACCENT_CYAN;

                SimpleAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setForeground(attr, color);
                StyleConstants.setFontFamily(attr, "Monospaced");
                StyleConstants.setFontSize(attr, 11);

                logDoc.insertString(logDoc.getLength(), line + "\n", attr);
                logPane.setCaretPosition(logDoc.getLength());
            } catch (BadLocationException ignored) {}
        });
    }

    public void updateUserList(List<String> users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String u : users) userListModel.addElement(u);
            clientCountLabel.setText("Clients: " + users.size());
        });
    }

    // ─── Helpers ─────────────────────────────────────────────────────
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

    // ─── Custom User List Cell Renderer ─────────────────────────────
    private class UserListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            label.setText("  ● " + value);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setForeground(isSelected ? Color.WHITE : ACCENT_GREEN);
            label.setBackground(isSelected ? new Color(0, 100, 70) : BG_PANEL);
            label.setBorder(new EmptyBorder(4, 6, 4, 6));
            return label;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(ServerGUI::new);
    }
}