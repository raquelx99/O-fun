package ui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

import javax.swing.*;

public class MainMenuPanel extends JPanel {

    private final MainWindow mainWindow;

    private final float[]  starX, starY, starSize, starAlpha, starSpeed;
    private static final int STAR_COUNT = 80;
    private Timer animTimer;

    public MainMenuPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BG);
        setOpaque(true);

        Random rng = new Random(7);
        starX     = new float[STAR_COUNT];
        starY     = new float[STAR_COUNT];
        starSize  = new float[STAR_COUNT];
        starAlpha = new float[STAR_COUNT];
        starSpeed = new float[STAR_COUNT];
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i]     = rng.nextFloat();
            starY[i]     = rng.nextFloat();
            starSize[i]  = 1 + rng.nextFloat() * 3;
            starAlpha[i] = 0.2f + rng.nextFloat() * 0.8f;
            starSpeed[i] = 0.003f + rng.nextFloat() * 0.007f;
        }

        animTimer = new Timer(60, e -> {
            for (int i = 0; i < STAR_COUNT; i++) {
                starAlpha[i] += starSpeed[i];
                if (starAlpha[i] > 1.0f || starAlpha[i] < 0.15f) starSpeed[i] = -starSpeed[i];
            }
            repaint();
        });
        animTimer.start();

        add(criarTopo(),   BorderLayout.NORTH);
        add(criarCentro(), BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        g2.setPaint(new GradientPaint(0, 0, new Color(14, 16, 28), 0, h, new Color(20, 12, 40)));
        g2.fillRect(0, 0, w, h);

        for (int i = 0; i < STAR_COUNT; i++) {
            float x = starX[i] * w;
            float y = starY[i] * h;
            float s = starSize[i];
            g2.setColor(new Color(1f, 1f, 1f, Math.min(1f, Math.max(0f, starAlpha[i]))));
            g2.fill(new java.awt.geom.Ellipse2D.Float(x - s / 2, y - s / 2, s, s));
        }

        drawGlow(g2, w * 0.2f, h * 0.3f, 260, new Color(90, 60, 200, 18));
        drawGlow(g2, w * 0.8f, h * 0.6f, 200, new Color(60, 120, 255, 15));
        drawGlow(g2, w * 0.5f, h * 0.8f, 180, new Color(200, 80, 255, 12));

        g2.dispose();
    }

    private void drawGlow(Graphics2D g2, float cx, float cy, float r, Color c) {
        RadialGradientPaint rg = new RadialGradientPaint(
            cx, cy, r,
            new float[]{0f, 1f},
            new Color[]{c, new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)}
        );
        g2.setPaint(rg);
        g2.fill(new java.awt.geom.Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
    }

    private JPanel criarTopo() {
        JPanel topo = new JPanel();
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));
        topo.setOpaque(false);
        topo.setBorder(BorderFactory.createEmptyBorder(52, 40, 10, 40));

        JLabel titulo = new JLabel("O(fun)");
        titulo.setFont(new Font("Poppins", Font.BOLD, 52));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Porque O(n²) é passado");
        sub.setFont(new Font("Poppins", Font.BOLD, 22));
        sub.setForeground(UiTheme.GOLD);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Compare algoritmos, venca desafios e analise desempenho");
        desc.setFont(UiTheme.BODY_FONT);
        desc.setForeground(UiTheme.TEXT_MUTED);
        desc.setAlignmentX(CENTER_ALIGNMENT);

        JLabel tags = new JLabel("Serial  *  Paralelo  *  Threads  *  CSV  *  Graficos");
        tags.setFont(new Font("Poppins", Font.BOLD, 12));
        tags.setForeground(UiTheme.BLUE_LIGHT);
        tags.setAlignmentX(CENTER_ALIGNMENT);

        topo.add(titulo);
        topo.add(Box.createVerticalStrut(6));
        topo.add(sub);
        topo.add(Box.createVerticalStrut(10));
        topo.add(desc);
        topo.add(Box.createVerticalStrut(6));
        topo.add(tags);

        return topo;
    }

    private JPanel criarCentro() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fill(new RoundRectangle2D.Float(4, 6, getWidth() - 4, getHeight() - 4, 32, 32));
                g2.setColor(new Color(26, 30, 52, 230));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 32, 32));
                g2.setColor(new Color(90, 110, 200, 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 32, 32));
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(28, 44, 28, 44));

        Object[][] botoes = {
            {"Laboratorio de Benchmark", "lab",        UiTheme.BLUE,                UiTheme.BLUE_DARK},
            {"Modo Desafio",              "desafio",    UiTheme.PURPLE,              UiTheme.PURPLE_DARK},
            {"Modo Aprenda",              "aprenda",    new Color(60, 180, 120),     new Color(40, 140, 90)},
            {"Resultados e Graficos",     "resultados", UiTheme.PANEL,               UiTheme.PANEL_HOVER},
            {"Sair",                      "sair",       UiTheme.PANEL,               UiTheme.PANEL_HOVER},
        };

        for (Object[] info : botoes) {
            JButton btn = UiTheme.makeRoundButton((String) info[0], (Color) info[2], (Color) info[3]);
            btn.setMaximumSize(new Dimension(360, 52));
            btn.setMinimumSize(new Dimension(280, 52));
            btn.setPreferredSize(new Dimension(340, 52));
            btn.setAlignmentX(CENTER_ALIGNMENT);

            if (info[2] == UiTheme.PANEL) btn.setForeground(UiTheme.TEXT);

            switch ((String) info[1]) {
                case "lab":        btn.addActionListener(e -> mainWindow.mostrarLaboratorio()); break;
                case "desafio":    btn.addActionListener(e -> mainWindow.mostrarDesafio());     break;
                case "aprenda":    btn.addActionListener(e -> mainWindow.mostrarAprenda());     break;
                case "resultados": btn.addActionListener(e -> mainWindow.mostrarResultados());  break;
                case "sair":       btn.addActionListener(e -> { animTimer.stop(); System.exit(0); }); break;
            }

            card.add(btn);
            card.add(Box.createVerticalStrut(10));
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 40, 10, 40);
        outer.add(card, gbc);
        return outer;
    }

    private JPanel criarRodape() {
        JPanel r = new JPanel();
        r.setOpaque(false);
        r.setBorder(BorderFactory.createEmptyBorder(6, 20, 16, 20));
        JLabel lbl = new JLabel("Analise de Desempenho de Algoritmos de Ordenacao - Java");
        lbl.setFont(UiTheme.SMALL_FONT);
        lbl.setForeground(new Color(70, 80, 110));
        r.add(lbl);
        return r;
    }
}
