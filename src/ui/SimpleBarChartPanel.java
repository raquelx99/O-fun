package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;

public class SimpleBarChartPanel extends JPanel {

    private static final Color[] CORES_BARRAS = {
        new Color(70, 130, 180),
        new Color(60, 179, 113),
        new Color(220, 100, 60),
        new Color(147, 112, 219),
        new Color(210, 180, 60),
        new Color(95, 190, 190),
        new Color(200, 90, 140),
        new Color(100, 160, 80),
    };

    private Map<String, Double> dados;
    private String titulo;
    private String eixoY;

    public SimpleBarChartPanel() {
        this.dados = new LinkedHashMap<>();
        this.titulo = "Gráfico";
        this.eixoY = "Valor";
        setPreferredSize(new Dimension(700, 320));
    }

    public void setDados(Map<String, Double> dados, String titulo, String eixoY) {
        this.dados = dados;
        this.titulo = titulo;
        this.eixoY = eixoY;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (dados == null || dados.isEmpty()) {
            desenharMensagem(g2, "Nenhum resultado disponível para gerar gráfico.");
            return;
        }

        int largura = getWidth();
        int altura = getHeight();

        int margemEsquerda = 75;
        int margemBaixo = 75;
        int margemTopo = 45;
        int margemDireita = 30;

        int areaLargura = largura - margemEsquerda - margemDireita;
        int areaAltura = altura - margemTopo - margemBaixo;

        double maiorValor = encontrarMaiorValor(dados);

        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Poppins", Font.BOLD, 15));
        g2.drawString(titulo, margemEsquerda, 25);

        g2.setFont(new Font("Poppins", Font.PLAIN, 11));
        g2.drawString(eixoY, 5, margemTopo + 10);

        g2.setColor(new Color(220, 220, 220));
        int divisoes = 4;
        for (int d = 1; d <= divisoes; d++) {
            int yGrade = (altura - margemBaixo) - (int)((d / (double) divisoes) * (areaAltura - 20));
            g2.drawLine(margemEsquerda, yGrade, largura - margemDireita, yGrade);

            g2.setColor(Color.GRAY);
            g2.setFont(new Font("Poppins", Font.PLAIN, 9));
            String valorGrade = String.format("%.1f", (maiorValor * d / divisoes));
            g2.drawString(valorGrade, 2, yGrade + 4);
            g2.setColor(new Color(220, 220, 220));
        }

        g2.setColor(Color.DARK_GRAY);
        g2.drawLine(margemEsquerda, altura - margemBaixo, largura - margemDireita, altura - margemBaixo);
        g2.drawLine(margemEsquerda, margemTopo, margemEsquerda, altura - margemBaixo);

        int quantidade = dados.size();
        int espacamento = 18;
        int larguraBarra = Math.max(25, (areaLargura - (quantidade + 1) * espacamento) / quantidade);

        int index = 0;

        for (Map.Entry<String, Double> entry : dados.entrySet()) {
            String rotulo = entry.getKey();
            double valor = entry.getValue();

            int alturaBarra = (int) ((valor / maiorValor) * (areaAltura - 20));
            int x = margemEsquerda + espacamento + index * (larguraBarra + espacamento);
            int y = altura - margemBaixo - alturaBarra;

            Color corBarra = CORES_BARRAS[index % CORES_BARRAS.length];
            g2.setColor(corBarra);
            g2.fillRect(x, y, larguraBarra, alturaBarra);

            g2.setColor(corBarra.darker());
            g2.drawRect(x, y, larguraBarra, alturaBarra);

            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("Poppins", Font.BOLD, 10));
            String valorFormatado = String.format("%.2f", valor);
            int xValor = x + Math.max(0, (larguraBarra - g2.getFontMetrics().stringWidth(valorFormatado)) / 2);
            g2.drawString(valorFormatado, xValor, y - 4);

            g2.setFont(new Font("Poppins", Font.PLAIN, 10));
            g2.setColor(Color.DARK_GRAY);
            String[] partes = rotulo.split(" ");
            int yRotulo = altura - margemBaixo + 14;
            for (String parte : partes) {
                int xRotulo = x + Math.max(0, (larguraBarra - g2.getFontMetrics().stringWidth(parte)) / 2);
                g2.drawString(parte, xRotulo, yRotulo);
                yRotulo += 12;
            }

            index++;
        }
    }

    private void desenharMensagem(Graphics2D g2, String mensagem) {
        g2.setColor(Color.GRAY);
        g2.setFont(new Font("Poppins", Font.PLAIN, 16));
        g2.drawString(mensagem, 30, 50);
    }

    private double encontrarMaiorValor(Map<String, Double> valores) {
        double maior = 0;

        for (double valor : valores.values()) {
            if (valor > maior) {
                maior = valor;
            }
        }

        if (maior <= 0) {
            maior = 1;
        }

        return maior;
    }
}