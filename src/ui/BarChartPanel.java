package ui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class BarChartPanel extends JPanel {

    private static final Color[] CORES_BARRAS = {
        new Color(90,  155, 255),  
        new Color(70,  210, 130),   
        new Color(255, 140,  70),   
        new Color(200, 100, 255),  
        new Color(255, 205,  70),  
        new Color(80,  200, 200),   
        new Color(255, 100, 150),  
        new Color(120, 190,  80),   
    };

    private static final int EXPORT_W = 1200;
    private static final int EXPORT_H = 650;

    private Map<String, Double> dados;
    private String titulo;
    private String eixoY;

    private final JButton salvarButton;

    public BarChartPanel() {
        this.dados  = new LinkedHashMap<>();
        this.titulo = "Grafico";
        this.eixoY  = "Valor";
        setPreferredSize(new Dimension(700, 320));
        setOpaque(false);
        setLayout(null);

        salvarButton = criarBotaoSalvar();
        add(salvarButton);
    }

    private JButton criarBotaoSalvar() {
        JButton btn = new JButton("Salvar PNG") {
            private boolean hovered = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited (java.awt.event.MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                Color bg = hovered ? new Color(90, 155, 255) : new Color(50, 60, 90);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
                g2.setColor(new Color(255, 255, 255, hovered ? 50 : 30));
                g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h / 2f, 12, 12));
                g2.setColor(new Color(255, 255, 255, 60));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 12, 12));
                g2.setFont(new Font("Arial", Font.BOLD, 11));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                g2.drawString(txt, (w - fm.stringWidth(txt)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };

        btn.setText("Salvar PNG");
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Salvar grafico como imagem PNG");
        btn.addActionListener(e -> salvarGrafico());
        return btn;
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int bw = 100, bh = 28;
        salvarButton.setBounds(getWidth() - bw - 8, 6, bw, bh);
    }

    public void setDados(Map<String, Double> dados, String titulo, String eixoY) {
        this.dados  = dados;
        this.titulo = titulo;
        this.eixoY  = eixoY;
        salvarButton.setEnabled(dados != null && !dados.isEmpty());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        configurarRenderizacao(g2);
        desenharGrafico(g2, getWidth(), getHeight(), UiTheme.PANEL_DARK);
        g2.dispose();
    }

    private void desenharGrafico(Graphics2D g2, int largura, int altura, Color bg) {
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, largura, altura, 16, 16));

        if (dados == null || dados.isEmpty()) {
            g2.setColor(new Color(150, 165, 200));
            g2.setFont(new Font("Arial", Font.PLAIN, 16));
            String msg = "Nenhum resultado disponivel para gerar grafico.";
            int mw = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (largura - mw) / 2, altura / 2);
            return;
        }

        int mL = 80, mB = 80, mT = 55, mR = 36;
        int aW = largura - mL - mR;
        int aH = altura  - mT - mB;
        double maiorValor = encontrarMaiorValor(dados);

        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(new Color(225, 232, 255));
        g2.drawString(titulo, mL, 32);

        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.setColor(new Color(150, 165, 200));
        g2.drawString(eixoY, 4, mT + 12);

        int divisoes = 5;
        for (int d = 1; d <= divisoes; d++) {
            int yG = (altura - mB) - (int)((d / (double) divisoes) * (aH - 20));

            g2.setColor(new Color(55, 65, 100, 120));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{4, 4}, 0));
            g2.drawLine(mL, yG, largura - mR, yG);

            g2.setStroke(new BasicStroke(1f));
            g2.setFont(new Font("Poppins", Font.PLAIN, 10));
            g2.setColor(new Color(130, 145, 180));
            g2.drawString(String.format("%.2f", maiorValor * d / divisoes), 2, yG + 4);
        }

        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(80, 92, 130));
        g2.drawLine(mL, altura - mB, largura - mR, altura - mB);
        g2.drawLine(mL, mT,          mL,           altura - mB);

        int qtd = dados.size();
        int esp = Math.max(10, 24 - qtd);
        int bW  = Math.max(20, (aW - (qtd + 1) * esp) / qtd);

        int idx = 0;
        for (Map.Entry<String, Double> entry : dados.entrySet()) {
            String  rotulo = entry.getKey();
            double  valor  = entry.getValue();
            int     bH     = (int)((valor / maiorValor) * (aH - 20));
            int     x      = mL + esp + idx * (bW + esp);
            int     y      = altura - mB - bH;

            Color c = CORES_BARRAS[idx % CORES_BARRAS.length];

            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillRoundRect(x + 3, y + 4, bW, bH, 8, 8);

            g2.setPaint(new GradientPaint(x, y, c.brighter(), x, y + bH, c.darker()));
            g2.fillRoundRect(x, y, bW, bH, 8, 8);

            g2.setColor(new Color(255, 255, 255, 55));
            g2.fillRoundRect(x + 2, y + 2, bW - 4, Math.min(12, bH / 2), 6, 6);

            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.setColor(new Color(225, 232, 255));
            String vFmt = String.format("%.2f", valor);
            int xv = x + Math.max(0, (bW - g2.getFontMetrics().stringWidth(vFmt)) / 2);
            g2.drawString(vFmt, xv, y - 5);

            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            g2.setColor(new Color(160, 172, 200));
            String[] partes = rotulo.split(" ");
            int yR = altura - mB + 14;
            for (String p : partes) {
                int xp = x + Math.max(0, (bW - g2.getFontMetrics().stringWidth(p)) / 2);
                g2.drawString(p, xp, yR);
                yR += 12;
            }

            idx++;
        }

        if (altura >= EXPORT_H) {
            String ts = "Gerado em " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            g2.setColor(new Color(80, 90, 120));
            g2.drawString(ts, mL, altura - 12);
        }
    }

    private void configurarRenderizacao(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,    RenderingHints.VALUE_STROKE_PURE);
    }

    private void salvarGrafico() {
        if (dados == null || dados.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nao ha dados para salvar.\nExecute um benchmark primeiro.",
                    "Sem dados", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nomeBase = titulo
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nomeSugerido = nomeBase + "_" + timestamp + ".png";

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salvar grafico como PNG");
        chooser.setSelectedFile(new File(nomeSugerido));
        chooser.setFileFilter(new FileNameExtensionFilter("Imagem PNG (*.png)", "png"));
        chooser.setAcceptAllFileFilterUsed(false);

        int opcao = chooser.showSaveDialog(this);
        if (opcao != JFileChooser.APPROVE_OPTION) return;

        File arquivo = chooser.getSelectedFile();
        if (!arquivo.getName().toLowerCase().endsWith(".png"))
            arquivo = new File(arquivo.getAbsolutePath() + ".png");

        if (arquivo.exists()) {
            int conf = JOptionPane.showConfirmDialog(this,
                    "O arquivo \"" + arquivo.getName() + "\" ja existe.\nDeseja substituir?",
                    "Confirmar substituicao", JOptionPane.YES_NO_OPTION);
            if (conf != JOptionPane.YES_OPTION) return;
        }

        exportarParaArquivo(arquivo);
    }

    private void exportarParaArquivo(File arquivo) {
        BufferedImage img = new BufferedImage(EXPORT_W, EXPORT_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        configurarRenderizacao(g2);
        desenharGrafico(g2, EXPORT_W, EXPORT_H, new Color(18, 22, 38));
        g2.dispose();

        try {
            ImageIO.write(img, "PNG", arquivo);

            salvarButton.setText("Salvo!");
            salvarButton.setEnabled(false);
            Timer reset = new Timer(2000, e -> {
                salvarButton.setText("Salvar PNG");
                salvarButton.setEnabled(true);
            });
            reset.setRepeats(false);
            reset.start();

            JOptionPane.showMessageDialog(this,
                    "Grafico salvo em:\n" + arquivo.getAbsolutePath(),
                    "Salvo com sucesso!", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double encontrarMaiorValor(Map<String, Double> valores) {
        double maior = 0;
        for (double v : valores.values()) if (v > maior) maior = v;
        return maior <= 0 ? 1 : maior;
    }
}
