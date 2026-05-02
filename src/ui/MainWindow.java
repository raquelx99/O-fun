package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class MainWindow extends JFrame {

    private static final int   DURACAO_MS  = 380;
    private static final int   FPS         = 60;
    private static final int   FRAME_MS    = 1000 / FPS;
    private static final float SLIDE_FRAC  = 0.28f;

    private JPanel       painelAtual;    
    private BufferedImage snapAntigo;      
    private BufferedImage snapNovo;      
    private Timer        timer;
    private long         startTime;
    private int          direcao;

    private final TransitionLayer camada;

    public MainWindow() {
        setTitle("AlgoQuest - Arena da Ordenacao");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 800);
        setLocationRelativeTo(null);
        setResizable(true);

        camada = new TransitionLayer();

        painelAtual = new MainMenuPanel(this);
        setContentPane(painelAtual);

        setVisible(true);
    }

    public void mostrarMenuPrincipal() {
        transicionarPara(new MainMenuPanel(this), -1);
    }

    public void mostrarLaboratorio() {
        transicionarPara(new LabPanel(this), +1);
    }

    public void mostrarDesafio() {
        transicionarPara(new ChallengePanel(this), +1);
    }

    public void mostrarResultados() {
        transicionarPara(new ResultsPanel(this), +1);
    }


    private void transicionarPara(JPanel novoPainel, int dir) {
        if (timer != null && timer.isRunning()) {
            timer.stop();
            finalizarTransicao(novoPainel);
            return;
        }

        direcao = dir;

        snapAntigo = capturar(painelAtual);

        novoPainel.setSize(getContentPane().getSize());
        novoPainel.doLayout();
        snapNovo = capturar(novoPainel);

        setContentPane(camada);
        camada.setSnapshots(snapAntigo, snapNovo);
        revalidate(); repaint();

        startTime = System.currentTimeMillis();
        timer = new Timer(FRAME_MS, e -> {
            float progress = (float)(System.currentTimeMillis() - startTime) / DURACAO_MS;
            if (progress >= 1f) {
                timer.stop();
                finalizarTransicao(novoPainel);
            } else {
                camada.setProgress(ease(progress), direcao);
            }
        });
        timer.start();
    }

    private void finalizarTransicao(JPanel novoPainel) {
        painelAtual = novoPainel;
        setContentPane(painelAtual);
        revalidate();
        repaint();
    }

    private BufferedImage capturar(JPanel painel) {
        Dimension size = getContentPane().getSize();
        if (size.width <= 0 || size.height <= 0) size = new Dimension(1150, 800);

        painel.setSize(size);
        painel.doLayout();

        layoutRecursivo(painel);

        BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,        RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        painel.paint(g2);
        g2.dispose();
        return img;
    }

    private void layoutRecursivo(java.awt.Container c) {
        c.doLayout();
        for (Component child : c.getComponents()) {
            if (child instanceof java.awt.Container)
                layoutRecursivo((java.awt.Container) child);
        }
    }

    private float ease(float t) {
        float inv = 1f - t;
        return 1f - inv * inv * inv;
    }

    public static void iniciar() {
        SwingUtilities.invokeLater(() -> new MainWindow());
    }

    private static class TransitionLayer extends JPanel {

        private BufferedImage snapAntigo;
        private BufferedImage snapNovo;
        private float         progress  = 0f;
        private int           direcao   = +1;

        TransitionLayer() {
            setOpaque(true);
            setBackground(UiTheme.BG);
        }

        void setSnapshots(BufferedImage antigo, BufferedImage novo) {
            this.snapAntigo = antigo;
            this.snapNovo   = novo;
            this.progress   = 0f;
        }

        void setProgress(float p, int dir) {
            this.progress = p;
            this.direcao  = dir;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (snapAntigo == null || snapNovo == null) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth(), h = getHeight();
            int maxSlide = (int)(w * SLIDE_FRAC);

            int xSai = (int)(-direcao * maxSlide * progress);
            float alphaSai = 1f - progress;

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, alphaSai)));
            g2.drawImage(snapAntigo, xSai, 0, w, h, null);

            int xEntra = (int)(direcao * maxSlide * (1f - progress));
            float alphaEntra = progress;

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alphaEntra)));
            g2.drawImage(snapNovo, xEntra, 0, w, h, null);

            g2.dispose();
        }
    }
}
