package ui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.Map;

import javax.swing.*;

import benchmark.DatasetGenerator;
import game.ChallengeGenerator;
import game.ChallengeResult;
import game.ChallengeRunner;
import game.ChallengeScenario;
import sort.*;

public class ChallengePanel extends JPanel {

    private final MainWindow         mainWindow;
    private final ChallengeGenerator challengeGenerator;
    private final ChallengeRunner    challengeRunner;
    private ChallengeScenario        scenarioAtual;

    private SortingPreviewPanel previewPanel;
    private int[]               previewAntes;

    private JLabel            tituloCenarioLabel;
    private JTextArea         descricaoArea;
    private JComboBox<String> algoritmoCombo;
    private JButton           executarButton;
    private JButton           novoDesafioButton;
    private JTextArea         resultadoArea;
    private JLabel            statusLabel;
    private ScoreBarPanel     scoreBar;

    public ChallengePanel(MainWindow mainWindow) {
        this.mainWindow         = mainWindow;
        this.challengeGenerator = new ChallengeGenerator();
        this.challengeRunner    = new ChallengeRunner();

        setLayout(new BorderLayout(0, 0));
        setBackground(UiTheme.BG);

        add(criarTopo(),  BorderLayout.NORTH);
        add(criarCorpo(), BorderLayout.CENTER);

        gerarNovoDesafio();
    }

    private JPanel criarTopo() {
        JPanel topo = new JPanel(new BorderLayout(10, 0));
        topo.setOpaque(false);
        topo.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));

        JButton voltar = new JButton("< Voltar");
        UiTheme.styleSecondaryButton(voltar);
        voltar.addActionListener(e -> mainWindow.mostrarMenuPrincipal());

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);

        JLabel titulo = new JLabel("Modo Desafio");
        titulo.setFont(new Font("Poppins", Font.BOLD, 22));
        titulo.setForeground(UiTheme.TEXT);
        titulo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Escolha o melhor algoritmo para cada cenario e acumule pontos!");
        sub.setFont(UiTheme.SMALL_FONT);
        sub.setForeground(UiTheme.TEXT_MUTED);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(sub);

        topo.add(voltar,  BorderLayout.WEST);
        topo.add(textos,  BorderLayout.CENTER);
        return topo;
    }

    private JSplitPane criarCorpo() {
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                criarPainelCenario(),
                criarPainelResultado()
        );
        split.setResizeWeight(0.45);
        split.setDividerSize(6);
        split.setOpaque(false);
        split.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        split.setBackground(UiTheme.BG);
        return split;
    }

    private JPanel criarPainelCenario() {
        JPanel card = UiTheme.makeRoundPanel(UiTheme.PANEL, UiTheme.RADIUS);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;

        tituloCenarioLabel = new JLabel("...");
        tituloCenarioLabel.setFont(new Font("Poppins", Font.BOLD, 17));
        tituloCenarioLabel.setForeground(UiTheme.GOLD);
        tituloCenarioLabel.setHorizontalAlignment(JLabel.CENTER);
        gc.gridy = 0; gc.insets = new Insets(0, 0, 8, 0);
        card.add(tituloCenarioLabel, gc);

        descricaoArea = new JTextArea(4, 0);
        descricaoArea.setEditable(false);
        descricaoArea.setLineWrap(true);
        descricaoArea.setWrapStyleWord(true);
        descricaoArea.setFont(UiTheme.BODY_FONT);
        descricaoArea.setBackground(UiTheme.PANEL_DARK);
        descricaoArea.setForeground(UiTheme.TEXT);
        descricaoArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane scrollDesc = new JScrollPane(descricaoArea);
        UiTheme.styleScrollPane(scrollDesc);
        scrollDesc.setPreferredSize(new Dimension(0, 90));
        gc.gridy = 1; gc.insets = new Insets(0, 0, 10, 0);
        card.add(scrollDesc, gc);

        previewPanel = new SortingPreviewPanel();
        previewPanel.setPreferredSize(new Dimension(0, 150));
        previewPanel.setMinimumSize(new Dimension(0, 100));
        JPanel wrapPrev = UiTheme.makeRoundPanel(UiTheme.PANEL_DARK, 12);
        wrapPrev.setLayout(new BorderLayout());
        wrapPrev.setBorder(UiTheme.createSectionBorder("Previa do cenario"));
        wrapPrev.add(previewPanel, BorderLayout.CENTER);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 12, 0);
        card.add(wrapPrev, gc);

        JLabel lblEsc = new JLabel("Escolha seu algoritmo:");
        lblEsc.setFont(UiTheme.SMALL_FONT);
        lblEsc.setForeground(UiTheme.TEXT_MUTED);
        gc.gridy = 3; gc.insets = new Insets(0, 0, 4, 0);
        card.add(lblEsc, gc);

        algoritmoCombo = new JComboBox<>(new String[]{"Bubble Sort","Insertion Sort","Quick Sort","Merge Sort"});
        UiTheme.styleCombo(algoritmoCombo);
        gc.gridy = 4; gc.insets = new Insets(0, 0, 10, 0);
        card.add(algoritmoCombo, gc);

        JPanel botoes = new JPanel(new GridBagLayout());
        botoes.setOpaque(false);
        GridBagConstraints bg = new GridBagConstraints();
        bg.fill = GridBagConstraints.HORIZONTAL;

        novoDesafioButton = new JButton("Novo desafio");
        UiTheme.styleSecondaryButton(novoDesafioButton);
        novoDesafioButton.addActionListener(e -> gerarNovoDesafio());
        bg.gridx = 0; bg.weightx = 0.45; bg.insets = new Insets(0, 0, 0, 5);
        botoes.add(novoDesafioButton, bg);

        executarButton = new JButton("Executar!");
        UiTheme.stylePrimaryButton(executarButton);
        executarButton.addActionListener(e -> executarEscolha());
        bg.gridx = 1; bg.weightx = 0.55; bg.insets = new Insets(0, 5, 0, 0);
        botoes.add(executarButton, bg);

        gc.gridy = 5; gc.insets = new Insets(0, 0, 8, 0);
        card.add(botoes, gc);

        scoreBar = new ScoreBarPanel();
        scoreBar.setPreferredSize(new Dimension(0, 36));
        gc.gridy = 6; gc.insets = new Insets(0, 0, 4, 0);
        card.add(scoreBar, gc);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UiTheme.SMALL_FONT);
        statusLabel.setForeground(UiTheme.GOLD);
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        gc.gridy = 7; gc.insets = new Insets(0, 0, 0, 0);
        card.add(statusLabel, gc);

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0; filler.gridy = 8; filler.weighty = 1;
        filler.fill = GridBagConstraints.VERTICAL;
        card.add(new JPanel() {{ setOpaque(false); }}, filler);

        return card;
    }

    private JPanel criarPainelResultado() {
        resultadoArea = new JTextArea();
        resultadoArea.setEditable(false);
        UiTheme.styleTextArea(resultadoArea);

        JScrollPane scroll = new JScrollPane(resultadoArea);
        UiTheme.styleScrollPane(scroll);

        JPanel wrap = UiTheme.makeRoundPanel(UiTheme.PANEL_DARK, UiTheme.RADIUS);
        wrap.setLayout(new BorderLayout());
        wrap.setBorder(UiTheme.createSectionBorder("Resultado do Desafio"));
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private void gerarNovoDesafio() {
        scenarioAtual = challengeGenerator.sortearCenario();
        tituloCenarioLabel.setText(scenarioAtual.getTitulo());
        descricaoArea.setText(scenarioAtual.getDescricao()
                + "\n\nTamanho: " + scenarioAtual.getTamanhoEntrada()
                + "    Tipo: " + scenarioAtual.getTipoEntrada());
        resultadoArea.setText("Escolha um algoritmo e clique em Executar!\n");
        scoreBar.setScore(0);
        if (statusLabel != null) statusLabel.setText(" ");
        gerarPreviaDoCenario();
    }

    private void gerarPreviaDoCenario() {
        previewAntes = DatasetGenerator.generate(24, scenarioAtual.getTipoEntrada());
        int[] depois = Arrays.copyOf(previewAntes, previewAntes.length);
        Arrays.sort(depois);
        previewPanel.atualizar(previewAntes, depois,
                scenarioAtual.getTitulo() + " - " + scenarioAtual.getTipoEntrada());
    }

    private void executarEscolha() {
        executarButton.setEnabled(false);
        novoDesafioButton.setEnabled(false);
        statusLabel.setForeground(UiTheme.BLUE);
        statusLabel.setText("Calculando...");

        String alg = (String) algoritmoCombo.getSelectedItem();

        if (previewAntes != null && previewAntes.length > 0) {
            int[] depois = Arrays.copyOf(previewAntes, previewAntes.length);
            criarAlgSerial(alg).sort(depois);
            previewPanel.atualizar(previewAntes, depois, "Previa com " + alg);
        }

        resultadoArea.setText("Executando " + alg + " no cenario \"" + scenarioAtual.getTitulo() + "\"...\n");

        SwingWorker<ChallengeResult, Void> worker = new SwingWorker<>() {
            @Override protected ChallengeResult doInBackground() {
                return challengeRunner.executarDesafio(scenarioAtual, alg);
            }
            @Override protected void done() {
                try {
                    mostrarResultado(get());
                    statusLabel.setForeground(UiTheme.SUCCESS);
                    statusLabel.setText("Concluido!");
                } catch (Exception e) {
                    resultadoArea.append("Erro: " + e.getMessage() + "\n");
                    statusLabel.setForeground(UiTheme.DANGER);
                    statusLabel.setText("Erro.");
                } finally {
                    executarButton.setEnabled(true);
                    novoDesafioButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void mostrarResultado(ChallengeResult r) {
        int pts = r.getPontuacao();
        scoreBar.animateToScore(pts);

        boolean ganhou = r.getAlgoritmoEscolhido().equals(r.getMelhorAlgoritmo());

        resultadoArea.setText("");
        resultadoArea.append("========================================\n");

        if      (ganhou)           resultadoArea.append("EXCELENTE! Voce escolheu o melhor!\n");
        else if (pts >= 700)       resultadoArea.append("Muito bom! Quase perfeito.\n");
        else if (pts >= 400)       resultadoArea.append("Razoavel. Havia opcao mais eficiente.\n");
        else                       resultadoArea.append("Pouco eficiente para este cenario.\n");

        resultadoArea.append("========================================\n\n");
        resultadoArea.append(String.format("  Sua escolha   : %s\n", r.getAlgoritmoEscolhido()));
        resultadoArea.append(String.format("  Melhor opcao  : %s\n", r.getMelhorAlgoritmo()));
        resultadoArea.append(String.format("  Seu tempo     : %.4f ms\n", r.getTempoEscolhido()));
        resultadoArea.append(String.format("  Melhor tempo  : %.4f ms\n\n", r.getMelhorTempo()));
        resultadoArea.append(String.format("  Pontuacao: %d / 1000\n\n", pts));

        resultadoArea.append("  Tempos por algoritmo:\n");
        resultadoArea.append("  ---------------------\n");
        for (Map.Entry<String, Double> e : r.getTemposPorAlgoritmo().entrySet()) {
            boolean melhor = e.getKey().equals(r.getMelhorAlgoritmo());
            resultadoArea.append(String.format("  %-18s : %8.4f ms%s\n",
                    e.getKey(), e.getValue(), melhor ? " <<" : ""));
        }
        resultadoArea.append("========================================\n");
    }

    private SortAlgorithm criarAlgSerial(String n) {
        switch (n) {
            case "Bubble Sort":    return new BubbleSort();
            case "Insertion Sort": return new InsertionSort();
            case "Quick Sort":     return new QuickSort();
            default:               return new MergeSort();
        }
    }

    private static class ScoreBarPanel extends JPanel {
        private int score = 0;
        private int displayScore = 0;
        private Timer anim;

        ScoreBarPanel() {
            setOpaque(false);
        }

        void setScore(int s) { this.score = s; this.displayScore = s; repaint(); }

        void animateToScore(int target) {
            score = target;
            if (anim != null) anim.stop();
            anim = new Timer(16, e -> {
                int diff = score - displayScore;
                if (Math.abs(diff) <= 5) { displayScore = score; ((Timer)e.getSource()).stop(); }
                else displayScore += diff / 8;
                repaint();
            });
            anim.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int barH = 18, barY = (h - barH) / 2;

            g2.setColor(UiTheme.PANEL_DARK);
            g2.fill(new RoundRectangle2D.Float(0, barY, w, barH, barH, barH));

            float ratio = Math.min(1f, displayScore / 1000f);
            int fillW = (int)(w * ratio);
            if (fillW > 0) {
                Color c1 = displayScore >= 700 ? UiTheme.SUCCESS :
                           displayScore >= 400 ? UiTheme.GOLD    : UiTheme.DANGER;
                Color c2 = c1.brighter();
                g2.setPaint(new GradientPaint(0, 0, c1, fillW, 0, c2));
                g2.fill(new RoundRectangle2D.Float(0, barY, fillW, barH, barH, barH));
            }

            g2.setColor(UiTheme.BORDER_COLOR);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0, barY, w - 1, barH, barH, barH));

            String txt = displayScore + " / 1000";
            g2.setFont(new Font("Poppins", Font.BOLD, 11));
            g2.setColor(UiTheme.TEXT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(txt, (w - fm.stringWidth(txt)) / 2, barY + barH - 4);

            g2.dispose();
        }
    }
}
