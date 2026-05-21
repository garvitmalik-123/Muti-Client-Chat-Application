


import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

public class Main {

    private static final Color BG_DARK      = new Color(18, 18, 28);
    private static final Color BG_PANEL     = new Color(26, 26, 40);
    private static final Color BG_INPUT     = new Color(35, 35, 52);
    private static final Color ACCENT_CYAN  = new Color(0, 200, 220);
    private static final Color ACCENT_GREEN = new Color(0, 210, 120);
    private static final Color ACCENT_PURPLE= new Color(160, 80, 220);
    private static final Color TEXT_WHITE   = new Color(220, 220, 235);
    private static final Color TEXT_GRAY    = new Color(130, 130, 160);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(Main::showLauncher);
    }

    private static void showLauncher() {
        JFrame frame = new JFrame("JavaChat — Launcher");
        frame.setSize(480, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.getContentPane().setBackground(BG_DARK);
        frame.setLayout(new BorderLayout());

        // ── Header ───────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setBackground(BG_PANEL);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(24, 20, 18, 20));

        JLabel logo = new JLabel("◈ JavaChat", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        logo.setForeground(ACCENT_CYAN);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("LAN Multi-Client Chat Application", SwingConstants.CENTER);
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagline.setForeground(TEXT_GRAY);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Show local IP
        String ip = "unknown";
        try { ip = InetAddress.getLocalHost().getHostAddress(); } catch (Exception ignored) {}
        JLabel ipLabel = new JLabel("Your LAN IP:  " + ip, SwingConstants.CENTER);
        ipLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ipLabel.setForeground(ACCENT_GREEN);
        ipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(logo);
        header.add(Box.createVerticalStrut(6));
        header.add(tagline);
        header.add(Box.createVerticalStrut(10));
        header.add(ipLabel);

        frame.add(header, BorderLayout.NORTH);

        // ── Center: Mode selection ────────────────────────────────────
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BG_DARK);
        center.setBorder(new EmptyBorder(10, 30, 10, 30));

        JPanel cards = new JPanel(new GridLayout(1, 2, 20, 0));
        cards.setBackground(BG_DARK);

        // Server card
        JPanel serverCard = makeCard(
                "🖥",
                "Start as Server",
                "Host the chat room.\nShare your IP with others.",
                ACCENT_CYAN,
                e -> {
                    frame.dispose();
                    new ServerGUI();
                });

        // Client card
        JPanel clientCard = makeCard(
                "💬",
                "Join as Client",
                "Connect to a server.\nEnter the host's IP.",
                ACCENT_PURPLE,
                e -> {
                    frame.dispose();
                    new ClientGUI();
                });

        cards.add(serverCard);
        cards.add(clientCard);
        center.add(cards);
        frame.add(center, BorderLayout.CENTER);

        // ── Footer ───────────────────────────────────────────────────
        JPanel footer = new JPanel();
        footer.setBackground(BG_PANEL);
        footer.setBorder(new EmptyBorder(10, 0, 10, 0));
        JLabel footerLabel = new JLabel("Core Java  |  Swing UI  |  LAN Chat  |  Port 5000");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(TEXT_GRAY);
        footer.add(footerLabel);
        frame.add(footer, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private static JPanel makeCard(String icon, String title, String desc,
                                   Color accent, ActionListener action) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent.darker(), 2),
                new EmptyBorder(20, 16, 20, 16)));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(accent);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel("<html><center>" + desc.replace("\n", "<br>") + "</center></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(TEXT_GRAY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btn = new JButton("Launch →");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(accent);
        btn.setBackground(BG_INPUT);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent.darker(), 1),
                new EmptyBorder(7, 18, 7, 18)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(action);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(50, 50, 75)); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(BG_INPUT); }
        });

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(descLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(btn);

        return card;
    }
}