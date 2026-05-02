package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Arrays;

import javax.swing.JPanel;

public class SortingPreviewPanel extends JPanel {

    private int[] valoresAntes;
    private int[] valoresDepois;
    private String titulo;

    private static final Color COR_ANTES  = new Color(220, 120, 60);
    private static final Color COR_DEPOIS = new Color(80, 210, 140);

    public SortingPreviewPanel() {
        this.valoresAntes  = new int[0];
        this.valoresDepois = new int[0];
        this.titulo = "Prévia visual da ordenação";
        setBackground(UiTheme.PANEL_DARK);
    }

    public void atualizar(int[] valoresAntes, int[] valoresDepois, String titulo) {
        this.valoresAntes  = Arrays.copyOf(valoresAntes, valoresAntes.length);
        this.valoresDepois = Arrays.copyOf(valoresDepois, valoresDepois.length);
        this.titulo = titulo;
        repaint();
    }

    public void limpar() {
        this.valoresAntes  = new int[0];
        this.valoresDepois = new int[0];
        this.titulo = "Prévia visual da ordenação";
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (valoresAntes.length == 0) {
            g2.setColor(UiTheme.TEXT_MUTED);
            g2.setFont(UiTheme.BODY_FONT);
            String msg = "Execute um benchmark para visualizar a ordenação.";
            int msgW = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (w - msgW) / 2, h / 2);
            return;
        }

        g2.setColor(UiTheme.TEXT);
        g2.setFont(new Font("Poppins", Font.BOLD, 13));
        g2.drawString(titulo, 12, 18);

        int margemH = 30;  
        int margemV = 14;  
        int metade  = (w - margemV) / 2;

        int areaAltura = h - margemH - 24;

        desenharConjunto(g2, valoresAntes,  "Antes →",  0,        metade, margemH, areaAltura, COR_ANTES);
        desenharConjunto(g2, valoresDepois, "→ Depois", metade + margemV, metade, margemH, areaAltura, COR_DEPOIS);
    }

    private void desenharConjunto(
            Graphics2D g2,
            int[] valores,
            String rotulo,
            int xOffset,
            int areaW,
            int yTopo,
            int areaH,
            Color cor
    ) {
        int n = valores.length;
        if (n == 0) return;

        int maiorValor = encontrarMaior(valores);

        g2.setColor(cor.brighter());
        g2.setFont(new Font("Poppins", Font.BOLD, 12));
        g2.drawString(rotulo, xOffset + 4, yTopo - 4);

        int espaco = Math.max(1, 3 - n / 10);
        int larguraBarra = Math.max(3, (areaW - (n + 1) * espaco) / n);

        for (int i = 0; i < n; i++) {
            int alturaBarra = (int) ((valores[i] / (double) maiorValor) * (areaH - 10));
            alturaBarra = Math.max(2, alturaBarra);

            int x = xOffset + espaco + i * (larguraBarra + espaco);
            int y = yTopo + (areaH - alturaBarra);

            g2.setColor(cor);
            g2.fillRect(x, y, larguraBarra, alturaBarra);

            g2.setColor(new Color(255, 255, 255, 40));
            g2.fillRect(x, y, larguraBarra, Math.min(4, alturaBarra));

            if (n <= 25 && larguraBarra >= 8) {
                g2.setColor(UiTheme.TEXT_MUTED);
                g2.setFont(new Font("Poppins", Font.PLAIN, 9));
                g2.drawString(String.valueOf(valores[i]), x, y + alturaBarra + 12);
            }
        }
    }

    private int encontrarMaior(int[] valores) {
        int maior = 1;
        for (int v : valores) if (v > maior) maior = v;
        return maior;
    }
}
