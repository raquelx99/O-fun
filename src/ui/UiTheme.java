package ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class UiTheme {

    public static final Color BG           = new Color(14, 16, 28);
    public static final Color PANEL        = new Color(26, 30, 50);
    public static final Color PANEL_DARK   = new Color(18, 22, 38);
    public static final Color PANEL_HOVER  = new Color(38, 44, 70);
    public static final Color BORDER_COLOR = new Color(55, 65, 100);

    public static final Color BLUE        = new Color(90, 155, 255);
    public static final Color BLUE_DARK   = new Color(60, 115, 210);
    public static final Color BLUE_LIGHT  = new Color(140, 190, 255);

    public static final Color GOLD        = new Color(255, 205, 70);
    public static final Color GOLD_DARK   = new Color(210, 165, 40);

    public static final Color SUCCESS     = new Color(70, 210, 130);
    public static final Color DANGER      = new Color(255, 90, 90);

    public static final Color PURPLE      = new Color(160, 100, 255);
    public static final Color PURPLE_DARK = new Color(120, 70, 210);

    public static final Color TEXT        = new Color(225, 232, 255);
    public static final Color TEXT_MUTED  = new Color(150, 165, 200);

    public static final Color BACKGROUND    = BG;
    public static final Color ACCENT        = BLUE;
    public static final Color ACCENT_DARK   = BLUE_DARK;
    public static final Color ACCENT_2      = GOLD;
    public static final Color ACCENT_2_DARK = GOLD_DARK;

    public static Font TITLE_FONT    = new Font("Arial", Font.BOLD,  38);
    public static Font SUBTITLE_FONT = new Font("Arial", Font.BOLD,  20);
    public static Font BODY_FONT     = new Font("Arial", Font.PLAIN, 14);
    public static Font SMALL_FONT    = new Font("Arial", Font.PLAIN, 12);
    public static Font BUTTON_FONT   = new Font("Arial", Font.BOLD,  14);
    public static Font MONO_FONT     = new Font("Consolas", Font.PLAIN, 13);

    public static void applyFonts() {
        TITLE_FONT    = AppFonts.title();
        SUBTITLE_FONT = AppFonts.subtitle();
        BODY_FONT     = AppFonts.body();
        SMALL_FONT    = AppFonts.small();
        BUTTON_FONT   = AppFonts.button();
        MONO_FONT     = AppFonts.mono();
    }

    public static final int RADIUS = 16;

    public static void styleRootPanel(JPanel panel) {
        panel.setBackground(BG);
    }

    public static void styleTitle(JLabel l)    { l.setFont(TITLE_FONT);    l.setForeground(TEXT);       l.setHorizontalAlignment(JLabel.CENTER); }
    public static void styleSubtitle(JLabel l) { l.setFont(SUBTITLE_FONT); l.setForeground(GOLD);       l.setHorizontalAlignment(JLabel.CENTER); }
    public static void styleBodyLabel(JLabel l){ l.setFont(BODY_FONT);     l.setForeground(TEXT_MUTED); }
    public static void styleMutedLabel(JLabel l){ l.setFont(SMALL_FONT);   l.setForeground(TEXT_MUTED); }

    public static JButton makeRoundButton(String text, Color base, Color dark) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            private float   scale   = 1.0f;

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                    @Override public void mousePressed(MouseEvent e) { scale = 0.96f;   repaint(); }
                    @Override public void mouseReleased(MouseEvent e){ scale = 1.0f;    repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();

                if (scale < 1.0f) {
                    float dx = w * (1 - scale) / 2f;
                    float dy = h * (1 - scale) / 2f;
                    g2.translate(dx, dy);
                    g2.scale(scale, scale);
                }

                Color top = hovered ? dark.brighter() : base;
                Color bot = hovered ? dark             : dark;

                GradientPaint gp = new GradientPaint(0, 0, top, 0, h, bot);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, RADIUS * 2, RADIUS * 2));

                g2.setColor(new Color(255, 255, 255, hovered ? 30 : 50));
                g2.fill(new RoundRectangle2D.Float(2, 2, w - 4, h / 2f, RADIUS * 2, RADIUS * 2));

                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(getText())) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

                g2.setColor(new Color(0, 0, 0, 80));
                g2.drawString(getText(), tx + 1, ty + 1);
                g2.setColor(getForeground());
                g2.drawString(getText(), tx, ty);

                g2.dispose();
            }

            @Override public boolean isOpaque() { return false; }
        };

        btn.setFont(BUTTON_FONT);
        btn.setForeground(new Color(15, 18, 35));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void stylePrimaryButton  (JButton b) { replaceWithRound(b, BLUE,   BLUE_DARK); }
    public static void styleSecondaryButton(JButton b) { replaceWithRound(b, PANEL,  PANEL_HOVER); b.setForeground(TEXT); }
    public static void styleMenuButton     (JButton b) { replaceWithRound(b, PANEL,  PANEL_HOVER); b.setForeground(TEXT); }
    public static void styleGoldButton     (JButton b) { replaceWithRound(b, GOLD,   GOLD_DARK); }

    private static void replaceWithRound(JButton b, Color base, Color dark) {
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setFont(BUTTON_FONT);
        b.setForeground(base == PANEL ? TEXT : new Color(15, 18, 35));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        final Color normalBg = base;
        final Color hoverBg  = dark;
        final boolean lightText = (base == PANEL || base == PANEL_HOVER);

        b.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            private boolean hovered = false;

            {
                b.addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { if (b.isEnabled()) { hovered = true;  b.repaint(); }}
                    @Override public void mouseExited (MouseEvent e) {                      hovered = false; b.repaint(); }
                });
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton ab = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = ab.getWidth(), h = ab.getHeight();
                Color top = hovered ? hoverBg.brighter() : normalBg;
                Color bot = hovered ? hoverBg             : normalBg.darker();

                g2.setPaint(new GradientPaint(0, 0, top, 0, h, bot));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, RADIUS * 2, RADIUS * 2));

                g2.setColor(new Color(255, 255, 255, hovered ? 60 : 30));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, RADIUS * 2, RADIUS * 2));

                g2.setColor(new Color(255, 255, 255, hovered ? 25 : 45));
                g2.fill(new RoundRectangle2D.Float(3, 3, w - 6, h / 2f, RADIUS * 2, RADIUS * 2));

                g2.setFont(ab.getFont());
                FontMetrics fm = g2.getFontMetrics();
                String txt = ab.getText();
                int tx = (w - fm.stringWidth(txt)) / 2;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2;

                g2.setColor(new Color(0, 0, 0, 70));
                g2.drawString(txt, tx + 1, ty + 1);
                g2.setColor(lightText ? TEXT : new Color(15, 18, 35));
                g2.drawString(txt, tx, ty);

                g2.dispose();
            }
        });
    }

    public static Border roundBorder(Color color, int radius, int padding) {
        return new AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(x + 1, y + 1, w - 2, h - 2, radius * 2, radius * 2));
                g2.dispose();
            }
            @Override
            public Insets getBorderInsets(Component c) { return new Insets(padding, padding, padding, padding); }
            @Override
            public Insets getBorderInsets(Component c, Insets i) {
                i.set(padding, padding, padding, padding); return i;
            }
        };
    }

    public static JPanel makeRoundPanel(Color bg, int radius) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius * 2, radius * 2));
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
    }

    public static void styleCombo(JComboBox<?> combo) {
        combo.setBackground(PANEL_DARK);
        combo.setForeground(TEXT);
        combo.setFont(SMALL_FONT);
        combo.setBorder(roundBorder(BORDER_COLOR, 8, 4));
    }

    public static void styleTable(JTable table) {
        table.setBackground(PANEL_DARK);
        table.setForeground(TEXT);
        table.setFont(SMALL_FONT);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(BLUE_DARK);
        table.setSelectionForeground(TEXT);
        table.setRowHeight(26);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        JTableHeader header = table.getTableHeader();
        header.setBackground(PANEL);
        header.setForeground(GOLD);
        header.setFont(new Font("Poppins", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));

        DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
        cr.setHorizontalAlignment(JLabel.CENTER);
        cr.setBackground(PANEL_DARK);
        cr.setForeground(TEXT);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(cr);
    }

    public static void styleScrollPane(JScrollPane s) {
        s.setBackground(PANEL_DARK);
        s.getViewport().setBackground(PANEL_DARK);
        s.setBorder(roundBorder(BORDER_COLOR, RADIUS, 0));
    }

    public static void styleTextArea(JTextArea a) {
        a.setBackground(PANEL_DARK);
        a.setForeground(TEXT);
        a.setCaretColor(TEXT);
        a.setFont(MONO_FONT);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
    }

    public static Border createCardBorder() {
        return roundBorder(BORDER_COLOR, RADIUS, 16);
    }

    public static Border createSectionBorder(String title) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                roundBorder(BORDER_COLOR, RADIUS, 0),
                "  " + title + "  ",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Poppins", Font.BOLD, 11),
                TEXT_MUTED
            ),
            BorderFactory.createEmptyBorder(4, 6, 6, 6)
        );
    }

    public static void addHoverEffect(JButton b, Color normal, Color hover) {
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (b.isEnabled()) b.setBackground(hover); }
            @Override public void mouseExited (MouseEvent e) { b.setBackground(normal); }
        });
    }
}
