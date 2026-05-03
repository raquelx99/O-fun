package ui;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ChartPanel extends JPanel {

    public enum Tipo { BARRAS, BARRAS_AGRUPADAS, LINHAS }

    private Tipo   tipo   = Tipo.BARRAS;
    private String titulo = "Grafico";
    private String eixoY  = "Valor";

    private Map<String, Double> dadosSimples = new LinkedHashMap<>();

    private Map<String, Map<String, Double>> dadosAgrupados = new LinkedHashMap<>();
    private List<String> grupos = new ArrayList<>();

    private Map<String, List<double[]>> dadosLinhas = new LinkedHashMap<>();

    static final Color[] CORES = {
        new Color(90,  155, 255), new Color(70,  210, 130),
        new Color(255, 140,  70), new Color(200, 100, 255),
        new Color(255, 205,  70), new Color(80,  200, 200),
        new Color(255, 100, 150), new Color(120, 190,  80),
    };

    private static final int EXPORT_W = 1400, EXPORT_H = 700;

    private final JButton salvarButton;

    public ChartPanel() {
        setPreferredSize(new Dimension(700, 340));
        setOpaque(false);
        setLayout(null);

        salvarButton = criarBotaoSalvar();
        add(salvarButton);
    }

    public void setDados(Map<String, Double> dados, String titulo, String eixoY) {
        this.tipo         = Tipo.BARRAS;
        this.dadosSimples = dados != null ? dados : new LinkedHashMap<>();
        this.titulo       = titulo;
        this.eixoY        = eixoY;
        salvarButton.setEnabled(!this.dadosSimples.isEmpty());
        repaint();
    }

    public void setDadosAgrupados(Map<String, Map<String, Double>> dados,
                                   List<String> grupos,
                                   String titulo, String eixoY) {
        this.tipo           = Tipo.BARRAS_AGRUPADAS;
        this.dadosAgrupados = dados  != null ? dados  : new LinkedHashMap<>();
        this.grupos         = grupos != null ? grupos : new ArrayList<>();
        this.titulo         = titulo;
        this.eixoY          = eixoY;
        salvarButton.setEnabled(!this.dadosAgrupados.isEmpty());
        repaint();
    }

    public void setDadosLinhas(Map<String, List<double[]>> dados, String titulo, String eixoY) {
        this.tipo        = Tipo.LINHAS;
        this.dadosLinhas = dados != null ? dados : new LinkedHashMap<>();
        this.titulo      = titulo;
        this.eixoY       = eixoY;
        salvarButton.setEnabled(!this.dadosLinhas.isEmpty());
        repaint();
    }

    @Override public void doLayout() {
        super.doLayout();
        int bw = 100, bh = 28;
        salvarButton.setBounds(getWidth() - bw - 8, 6, bw, bh);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = setup(g);
        desenharGrafico(g2, getWidth(), getHeight(), UiTheme.PANEL_DARK, false);
        g2.dispose();
    }

    private void desenharGrafico(Graphics2D g2, int w, int h, Color bg, boolean exportando) {
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));

        boolean vazio = switch (tipo) {
            case BARRAS           -> dadosSimples.isEmpty();
            case BARRAS_AGRUPADAS -> dadosAgrupados.isEmpty();
            case LINHAS           -> dadosLinhas.isEmpty();
        };
        if (vazio) {
            g2.setColor(UiTheme.TEXT_MUTED);
            g2.setFont(new Font("Poppins", Font.PLAIN, 16));
            String msg = "Nenhum resultado disponivel para gerar grafico.";
            g2.drawString(msg, (w - g2.getFontMetrics().stringWidth(msg)) / 2, h / 2);
            return;
        }

        switch (tipo) {
            case BARRAS           -> desenharBarras(g2, w, h, exportando);
            case BARRAS_AGRUPADAS -> desenharBarrasAgrupadas(g2, w, h, exportando);
            case LINHAS           -> desenharLinhas(g2, w, h, exportando);
        }

        if (exportando) {
            g2.setFont(new Font("Poppins", Font.PLAIN, 11));
            g2.setColor(new Color(80, 90, 120));
            String ts = "Gerado em " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            g2.drawString(ts, 80, h - 10);
        }
    }

    private static final int ML = 80, MB = 80, MT = 50, MR = 36;

    private void desenharBarras(Graphics2D g2, int w, int h, boolean exp) {
        Map<String, Double> dados = dadosSimples;
        double maiorValor = dados.values().stream().mapToDouble(d -> d).max().orElse(1);

        desenharTitulo(g2, titulo, w);
        desenharEixoY(g2, eixoY);
        desenharGrade(g2, w, h, maiorValor, 5);
        desenharEixos(g2, w, h);

        int aW = w - ML - MR, aH = h - MT - MB;
        int n  = dados.size();
        int esp = Math.max(10, 24 - n);
        int bW  = Math.max(20, (aW - (n + 1) * esp) / n);
        int idx = 0;

        for (Map.Entry<String, Double> e : dados.entrySet()) {
            double v  = e.getValue();
            int    bH = (int)((v / maiorValor) * (aH - 20));
            int    x  = ML + esp + idx * (bW + esp);
            int    y  = h - MB - bH;
            Color  c  = CORES[idx % CORES.length];

            desenharBarra(g2, x, y, bW, bH, c);
            desenharValorBarra(g2, String.format("%.2f", v), x, y, bW);
            desenharRotuloX(g2, e.getKey(), x, bW, h - MB);
            idx++;
        }
    }

    private void desenharBarrasAgrupadas(Graphics2D g2, int w, int h, boolean exp) {
        if (grupos.isEmpty() || dadosAgrupados.isEmpty()) return;

        double maiorValor = 1;
        for (Map<String, Double> gMap : dadosAgrupados.values())
            for (double v : gMap.values()) if (v > maiorValor) maiorValor = v;

        desenharTitulo(g2, titulo, w);
        desenharEixoY(g2, eixoY);
        desenharGrade(g2, w, h, maiorValor, 5);
        desenharEixos(g2, w, h);

        int aW          = w - ML - MR;
        int aH          = h - MT - MB;
        int nCat        = dadosAgrupados.size();
        int nGrupos     = grupos.size();
        int espCat      = 14;
        int espBarra    = 3;
        int largCatTotal= (aW - (nCat + 1) * espCat) / nCat;
        int bW          = Math.max(8, (largCatTotal - (nGrupos - 1) * espBarra) / nGrupos);

        int catIdx = 0;
        for (Map.Entry<String, Map<String, Double>> catEntry : dadosAgrupados.entrySet()) {
            String categoriaNome = catEntry.getKey();
            int catX = ML + espCat + catIdx * (largCatTotal + espCat);

            int grupoIdx = 0;
            for (String grupo : grupos) {
                double v  = catEntry.getValue().getOrDefault(grupo, 0.0);
                int    bH = (int)((v / maiorValor) * (aH - 20));
                int    x  = catX + grupoIdx * (bW + espBarra);
                int    y  = h - MB - bH;
                Color  c  = CORES[grupoIdx % CORES.length];

                desenharBarra(g2, x, y, bW, bH, c);
                if (bW >= 18) desenharValorBarra(g2, String.format("%.1f", v), x, y, bW);
                grupoIdx++;
            }

            g2.setFont(new Font("Poppins", Font.PLAIN, 10));
            g2.setColor(UiTheme.TEXT_MUTED);
            String[] partes = categoriaNome.split(" ");
            int yR = h - MB + 13;
            int centroX = catX + largCatTotal / 2;
            for (String parte : partes) {
                int xP = centroX - g2.getFontMetrics().stringWidth(parte) / 2;
                g2.drawString(parte, xP, yR);
                yR += 11;
            }
            catIdx++;
        }

        desenharLegenda(g2, grupos, w, h);
    }

    private void desenharLinhas(Graphics2D g2, int w, int h, boolean exp) {
        if (dadosLinhas.isEmpty()) return;

        double minXMut = Double.MAX_VALUE, maxXMut = -Double.MAX_VALUE;
        double maxYMut = -Double.MAX_VALUE;
        for (List<double[]> pts : dadosLinhas.values()) {
            for (double[] pt : pts) {
                if (pt[0] < minXMut) minXMut = pt[0];
                if (pt[0] > maxXMut) maxXMut = pt[0];
                if (pt[1] > maxYMut) maxYMut = pt[1];
            }
        }
        if (maxXMut <= minXMut) maxXMut = minXMut + 1;
        if (maxYMut <= 0)       maxYMut = 1;

        final double minX = minXMut, maxX = maxXMut, maxY = maxYMut;

        desenharTitulo(g2, titulo, w);
        desenharEixoY(g2, eixoY);
        desenharGrade(g2, w, h, maxY, 5);
        desenharEixos(g2, w, h);

        int aW = w - ML - MR;
        int aH = h - MT - MB;

        java.util.function.Function<Double, Integer> toPixX =
            x -> (int)(ML + (x - minX) / (maxX - minX) * (aW - 10));
        java.util.function.Function<Double, Integer> toPixY =
            y -> (int)(h - MB - (y / maxY) * (aH - 20));

        TreeSet<Double> xVals = new TreeSet<>();
        for (List<double[]> pts : dadosLinhas.values())
            for (double[] pt : pts) xVals.add(pt[0]);

        g2.setFont(new Font("Poppins", Font.PLAIN, 10));
        g2.setColor(UiTheme.TEXT_MUTED);
        for (double xv : xVals) {
            int px = toPixX.apply(xv);
            String lbl = xv >= 1_000_000 ? (int)(xv/1_000_000) + "M"
                       : xv >= 1_000     ? (int)(xv/1_000)     + "k"
                       : String.valueOf((int)xv);
            g2.drawString(lbl, px - g2.getFontMetrics().stringWidth(lbl) / 2, h - MB + 14);

            g2.setColor(new Color(55, 65, 100, 60));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{3, 3}, 0));
            g2.drawLine(px, MT, px, h - MB);
            g2.setColor(UiTheme.TEXT_MUTED);
            g2.setStroke(new BasicStroke(1f));
        }

        int serieIdx = 0;
        List<String> serieNomes = new ArrayList<>(dadosLinhas.keySet());
        for (String serie : serieNomes) {
            List<double[]> pts = dadosLinhas.get(serie);
            if (pts.size() < 2) { serieIdx++; continue; }

            pts.sort(Comparator.comparingDouble(p -> p[0]));

            Color cor = CORES[serieIdx % CORES.length];

            GeneralPath area = new GeneralPath();
            area.moveTo(toPixX.apply(pts.get(0)[0]), h - MB);
            for (double[] pt : pts) area.lineTo(toPixX.apply(pt[0]), toPixY.apply(pt[1]));
            area.lineTo(toPixX.apply(pts.get(pts.size()-1)[0]), h - MB);
            area.closePath();
            g2.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), 22));
            g2.fill(area);

            g2.setColor(cor);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 1; i < pts.size(); i++) {
                g2.drawLine(toPixX.apply(pts.get(i-1)[0]), toPixY.apply(pts.get(i-1)[1]),
                            toPixX.apply(pts.get(i)[0]),   toPixY.apply(pts.get(i)[1]));
            }

            g2.setStroke(new BasicStroke(1.5f));
            for (double[] pt : pts) {
                int px = toPixX.apply(pt[0]), py = toPixY.apply(pt[1]);
                g2.setColor(UiTheme.PANEL_DARK);
                g2.fillOval(px - 5, py - 5, 10, 10);
                g2.setColor(cor);
                g2.fillOval(px - 4, py - 4, 8, 8);

                if (xVals.size() <= 8) {
                    g2.setFont(new Font("Poppins", Font.BOLD, 9));
                    String v = pt[1] >= 1000 ? String.format("%.0f", pt[1])
                             : pt[1] >= 10   ? String.format("%.1f", pt[1])
                             :                  String.format("%.2f", pt[1]);
                    g2.drawString(v, px - g2.getFontMetrics().stringWidth(v) / 2, py - 8);
                }
            }
            serieIdx++;
        }

        desenharLegenda(g2, serieNomes, w, h);
    }

    private void desenharBarra(Graphics2D g2, int x, int y, int bW, int bH, Color c) {
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillRoundRect(x + 3, y + 4, bW, bH, 8, 8);
        g2.setPaint(new GradientPaint(x, y, c.brighter(), x, y + bH, c.darker()));
        g2.fillRoundRect(x, y, bW, bH, 8, 8);
        g2.setColor(new Color(255, 255, 255, 50));
        g2.fillRoundRect(x + 2, y + 2, bW - 4, Math.min(10, bH / 2), 6, 6);
    }

    private void desenharValorBarra(Graphics2D g2, String v, int x, int y, int bW) {
        g2.setFont(new Font("Poppins", Font.BOLD, 10));
        g2.setColor(UiTheme.TEXT);
        int xv = x + (bW - g2.getFontMetrics().stringWidth(v)) / 2;
        g2.setColor(new Color(0, 0, 0, 60)); g2.drawString(v, xv + 1, y - 3);
        g2.setColor(UiTheme.TEXT);           g2.drawString(v, xv,     y - 4);
    }

    private void desenharRotuloX(Graphics2D g2, String rotulo, int x, int bW, int baseY) {
        g2.setFont(new Font("Poppins", Font.PLAIN, 10));
        g2.setColor(UiTheme.TEXT_MUTED);
        String[] partes = rotulo.split(" ");
        int yR = baseY + 14;
        for (String p : partes) {
            int xP = x + (bW - g2.getFontMetrics().stringWidth(p)) / 2;
            g2.drawString(p, xP, yR);
            yR += 11;
        }
    }

    private void desenharTitulo(Graphics2D g2, String t, int w) {
        g2.setFont(new Font("Poppins", Font.BOLD, 15));
        g2.setColor(UiTheme.TEXT);
        g2.drawString(t, ML, 30);
    }

    private void desenharEixoY(Graphics2D g2, String label) {
        g2.setFont(new Font("Poppins", Font.PLAIN, 11));
        g2.setColor(UiTheme.TEXT_MUTED);
        g2.drawString(label, 4, MT + 10);
    }

    private void desenharGrade(Graphics2D g2, int w, int h, double maxVal, int div) {
        for (int d = 1; d <= div; d++) {
            int yG = (h - MB) - (int)((d / (double) div) * (h - MT - MB - 20));
            g2.setColor(new Color(55, 65, 100, 120));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{4, 4}, 0));
            g2.drawLine(ML, yG, w - MR, yG);
            g2.setStroke(new BasicStroke(1f));
            g2.setFont(new Font("Poppins", Font.PLAIN, 10));
            g2.setColor(new Color(130, 145, 180));
            double val = maxVal * d / div;
            String lbl = val >= 1_000_000 ? String.format("%.1fM", val/1_000_000)
                       : val >= 1_000     ? String.format("%.1fk", val/1_000)
                       :                    String.format("%.2f", val);
            g2.drawString(lbl, 2, yG + 4);
        }
    }

    private void desenharEixos(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(80, 92, 130));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(ML, h - MB, w - MR, h - MB);
        g2.drawLine(ML, MT,     ML,     h - MB);
    }

    private void desenharLegenda(Graphics2D g2, List<String> nomes, int w, int h) {
        int legW = 100;
        int totalLargura = nomes.size() * (legW + 8);
        int startX = (w - totalLargura) / 2;
        int y = h - 16;
        for (int i = 0; i < nomes.size(); i++) {
            Color c = CORES[i % CORES.length];
            int x = startX + i * (legW + 8);
            g2.setColor(c);
            g2.fillRoundRect(x, y - 9, 14, 10, 4, 4);
            g2.setFont(new Font("Poppins", Font.PLAIN, 10));
            g2.setColor(UiTheme.TEXT_MUTED);
            g2.drawString(truncar(g2, nomes.get(i), legW - 18), x + 18, y);
        }
    }

    private String truncar(Graphics2D g2, String s, int maxW) {
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(s) <= maxW) return s;
        while (s.length() > 3 && fm.stringWidth(s + "...") > maxW)
            s = s.substring(0, s.length() - 1);
        return s + "...";
    }

    private Graphics2D setup(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        return g2;
    }

    private JButton criarBotaoSalvar() {
        JButton btn = new JButton("Salvar PNG") {
            private boolean hov = false;
            { addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { hov = true;  repaint(); }
                public void mouseExited (java.awt.event.MouseEvent e) { hov = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int bw = getWidth(), bh = getHeight();
                g2.setColor(hov ? UiTheme.BLUE : new Color(50, 60, 90));
                g2.fill(new RoundRectangle2D.Float(0, 0, bw, bh, 12, 12));
                g2.setColor(new Color(255,255,255,30));
                g2.fill(new RoundRectangle2D.Float(1, 1, bw-2, bh/2f, 12, 12));
                g2.setFont(new Font("Poppins", Font.BOLD, 11));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (bw-fm.stringWidth(getText()))/2, (bh+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
            @Override public boolean isOpaque() { return false; }
        };
        btn.setText("Salvar PNG");
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> salvarGrafico());
        return btn;
    }

    private void salvarGrafico() {
        boolean vazio = switch (tipo) {
            case BARRAS           -> dadosSimples.isEmpty();
            case BARRAS_AGRUPADAS -> dadosAgrupados.isEmpty();
            case LINHAS           -> dadosLinhas.isEmpty();
        };
        if (vazio) {
            JOptionPane.showMessageDialog(this, "Nao ha dados para salvar.", "Sem dados", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nomeBase = titulo.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Salvar grafico como PNG");
        fc.setSelectedFile(new File(nomeBase + "_" + ts + ".png"));
        fc.setFileFilter(new FileNameExtensionFilter("Imagem PNG (*.png)", "png"));
        fc.setAcceptAllFileFilterUsed(false);
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File arquivo = fc.getSelectedFile();
        if (!arquivo.getName().toLowerCase().endsWith(".png"))
            arquivo = new File(arquivo.getAbsolutePath() + ".png");
        if (arquivo.exists()) {
            int conf = JOptionPane.showConfirmDialog(this, "Substituir \"" + arquivo.getName() + "\"?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (conf != JOptionPane.YES_OPTION) return;
        }
        try {
            BufferedImage img = new BufferedImage(EXPORT_W, EXPORT_H, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            setup(g2);
            desenharGrafico(g2, EXPORT_W, EXPORT_H, new Color(18, 22, 38), true);
            g2.dispose();
            ImageIO.write(img, "PNG", arquivo);
            salvarButton.setText("Salvo!");
            salvarButton.setEnabled(false);
            new javax.swing.Timer(2000, ev -> { salvarButton.setText("Salvar PNG"); salvarButton.setEnabled(true); }) {{
                setRepeats(false); start();
            }};
            JOptionPane.showMessageDialog(this, "Salvo em:\n" + arquivo.getAbsolutePath(), "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
