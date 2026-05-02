package ui;

import javax.swing.SwingWorker;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import benchmark.BenchmarkConfig;
import benchmark.BenchmarkRunner;
import benchmark.BenchmarkStats;
import benchmark.CsvExporter;
import benchmark.CsvResumoExporter;
import benchmark.DatasetGenerator;
import model.ResultadoBenchmark;
import model.TipoEntrada;
import parallel.ParallelBubbleSort;
import parallel.ParallelInsertionSort;
import parallel.ParallelMergeSort;
import parallel.ParallelQuickSort;
import parallel.ParallelSortAlgorithm;
import sort.BubbleSort;
import sort.InsertionSort;
import sort.MergeSort;
import sort.QuickSort;
import sort.SortAlgorithm;

public class LabPanel extends JPanel {

    private JComboBox<String>      algoritmoCombo;
    private JComboBox<String>      versaoCombo;
    private JComboBox<Integer>     threadsCombo;
    private JComboBox<Integer>     tamanhoCombo;
    private JComboBox<TipoEntrada> tipoEntradaCombo;
    private JComboBox<Integer>     amostrasCombo;
    private Map<String, Double>    mediasSeriais;

    private JButton   executarButton;
    private JButton   limparButton;
    private JTextArea resultadoArea;
    private JLabel    statusLabel;

    private BenchmarkRunner     runner;
    private MainWindow          mainWindow;
    private SortingPreviewPanel previewPanel;

    private final String arquivoCsvBruto  = "resultados_ui.csv";
    private final String arquivoCsvResumo = "resultados_resumo_ui.csv";

    public LabPanel(MainWindow mainWindow) {
        this.mainWindow    = mainWindow;
        this.runner        = new BenchmarkRunner();
        this.mediasSeriais = new HashMap<>();

        CsvUiInitializer.inicializar(arquivoCsvBruto, arquivoCsvResumo);

        setLayout(new BorderLayout(0, 0));
        setBackground(UiTheme.BG);

        add(criarTopo(),  BorderLayout.NORTH);
        add(criarCorpo(), BorderLayout.CENTER);
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

        JLabel titulo = makeHeader("Laboratorio de Benchmark");
        JLabel sub = new JLabel("Compare algoritmos seriais e paralelos");
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
                criarFormulario(),
                criarAreaResultados()
        );
        split.setResizeWeight(0.22);
        split.setDividerSize(6);
        split.setOpaque(false);
        split.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        split.setBackground(UiTheme.BG);
        split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                e -> { if ((int) e.getNewValue() < 220) split.setDividerLocation(220); });
        return split;
    }

    private JScrollPane criarFormulario() {
        JPanel card = makeCard();
        card.setLayout(new GridBagLayout());

        GridBagConstraints lc = gbc(0, 0, new Insets(6, 4, 1, 4));
        GridBagConstraints cc = gbc(0, 0, new Insets(0, 4, 8, 4));

        algoritmoCombo   = new JComboBox<>(new String[]{"Bubble Sort","Insertion Sort","Quick Sort","Merge Sort"});
        versaoCombo      = new JComboBox<>(new String[]{"Serial","Paralelo"});
        threadsCombo     = new JComboBox<>(new Integer[]{1,2,4,8});
        tamanhoCombo     = new JComboBox<>(new Integer[]{1000,5000,10000,50000});
        tipoEntradaCombo = new JComboBox<>(new TipoEntrada[]{
            TipoEntrada.ALEATORIA, TipoEntrada.ORDENADA,
            TipoEntrada.QUASE_ORDENADA, TipoEntrada.INVERTIDA, TipoEntrada.REPETIDA});
        amostrasCombo    = new JComboBox<>(new Integer[]{1,3,5,10});
        amostrasCombo.setSelectedItem(5);

        versaoCombo.addActionListener(e -> atualizarEstadoThreads());

        for (JComboBox<?> c : new JComboBox[]{algoritmoCombo, versaoCombo, threadsCombo,
                tamanhoCombo, tipoEntradaCombo, amostrasCombo})
            UiTheme.styleCombo(c);

        executarButton = new JButton("Executar Benchmark");
        UiTheme.stylePrimaryButton(executarButton);
        executarButton.addActionListener(e -> executarBenchmarkSelecionado());
        executarButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        limparButton = new JButton("Limpar resultados");
        UiTheme.styleSecondaryButton(limparButton);
        limparButton.addActionListener(e -> {
            resultadoArea.setText("");
            if (previewPanel != null) previewPanel.limpar();
            statusLabel.setText(" ");
        });
        limparButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UiTheme.SMALL_FONT);
        statusLabel.setForeground(UiTheme.GOLD);

        String[][] campos = {
            {"Algoritmo:",       "algo"},
            {"Versao:",          "versao"},
            {"Threads:",         "thr"},
            {"Tamanho:",         "tam"},
            {"Tipo de entrada:", "tipo"},
            {"Amostras:",        "am"},
        };
        JComboBox<?>[] combos = {algoritmoCombo, versaoCombo, threadsCombo, tamanhoCombo, tipoEntradaCombo, amostrasCombo};

        int row = 0;
        for (int i = 0; i < campos.length; i++) {
            lc.gridy = row++;
            JLabel lbl = makeFieldLabel(campos[i][0]);
            card.add(lbl, lc);
            cc.gridy = row++;
            card.add(combos[i], cc);
        }

        GridBagConstraints bc = gbc(0, row++, new Insets(8, 4, 4, 4));
        card.add(executarButton, bc);
        bc.gridy = row++;
        card.add(limparButton, bc);
        bc.gridy = row++;
        card.add(statusLabel, bc);

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0; filler.gridy = row; filler.weighty = 1;
        filler.fill = GridBagConstraints.VERTICAL;
        card.add(new JPanel() {{ setOpaque(false); }}, filler);

        atualizarEstadoThreads();

        JScrollPane scroll = new JScrollPane(card);
        scroll.setBorder(null);
        scroll.setMinimumSize(new Dimension(220, 0));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        return scroll;
    }

    private JPanel criarAreaResultados() {
        resultadoArea = new JTextArea();
        resultadoArea.setEditable(false);
        UiTheme.styleTextArea(resultadoArea);

        JScrollPane scrollRes = new JScrollPane(resultadoArea);
        UiTheme.styleScrollPane(scrollRes);
        scrollRes.setBorder(UiTheme.createSectionBorder("Resultados"));

        previewPanel = new SortingPreviewPanel();
        previewPanel.setMinimumSize(new Dimension(0, 120));

        JPanel wrapPrev = makeCard();
        wrapPrev.setLayout(new BorderLayout());
        wrapPrev.setBorder(UiTheme.createSectionBorder("Visualizacao - Antes vs Depois"));
        wrapPrev.add(previewPanel, BorderLayout.CENTER);

        JSplitPane sv = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollRes, wrapPrev);
        sv.setResizeWeight(0.65);
        sv.setDividerSize(5);
        sv.setBorder(null);
        sv.setOpaque(false);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(sv, BorderLayout.CENTER);
        return wrap;
    }

    private void atualizarEstadoThreads() {
        boolean p = "Paralelo".equals(versaoCombo.getSelectedItem());
        threadsCombo.setEnabled(p);
        if (!p) threadsCombo.setSelectedItem(1);
    }

    private void executarBenchmarkSelecionado() {
        executarButton.setEnabled(false);
        statusLabel.setForeground(UiTheme.BLUE);
        statusLabel.setText("Executando...");

        String      alg    = (String)      algoritmoCombo.getSelectedItem();
        String      versao = (String)      versaoCombo.getSelectedItem();
        int         thr    = (Integer)     threadsCombo.getSelectedItem();
        int         tam    = (Integer)     tamanhoCombo.getSelectedItem();
        TipoEntrada tipo   = (TipoEntrada) tipoEntradaCombo.getSelectedItem();
        int         am     = (Integer)     amostrasCombo.getSelectedItem();
        int         thrExib = versao.equals("Serial") ? 1 : thr;

        resultadoArea.append("=======================================\n");
        resultadoArea.append("  Algoritmo : " + alg + "\n");
        resultadoArea.append("  Versao    : " + versao + "\n");
        resultadoArea.append("  Threads   : " + thrExib + "\n");
        resultadoArea.append("  Tamanho   : " + tam + "\n");
        resultadoArea.append("  Entrada   : " + tipo + "\n");
        resultadoArea.append("  Amostras  : " + am + "\n");
        resultadoArea.append("---------------------------------------\n");

        if (versao.equals("Paralelo") && !mediasSeriais.containsKey(criarChave(alg, tam, tipo)))
            resultadoArea.append("Aviso: execute a versao serial primeiro para calcular speedup.\n");

        SwingWorker<List<ResultadoBenchmark>, Void> worker = new SwingWorker<>() {
            @Override protected List<ResultadoBenchmark> doInBackground() {
                BenchmarkConfig cfg = new BenchmarkConfig(tam, tipo, am);
                return versao.equals("Serial")
                    ? runner.runSerial(criarAlgSerial(alg), cfg)
                    : runner.runParallel(criarAlgParalelo(alg), cfg, thr);
            }
            @Override protected void done() {
                try {
                    List<ResultadoBenchmark> res = get();
                    imprimirResultados(res);
                    salvarCsv(res);
                    atualizarPrevia(alg, tipo);
                    resultadoArea.append("OK Resultado salvo nos CSVs.\n");
                    resultadoArea.append("=======================================\n\n");
                    statusLabel.setForeground(UiTheme.SUCCESS);
                    statusLabel.setText("Concluido!");
                } catch (Exception e) {
                    resultadoArea.append("ERRO: " + e.getMessage() + "\n");
                    statusLabel.setForeground(UiTheme.DANGER);
                    statusLabel.setText("Erro na execucao.");
                } finally {
                    executarButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void atualizarPrevia(String alg, TipoEntrada tipo) {
        int[] antes  = DatasetGenerator.generate(28, tipo);
        int[] depois = Arrays.copyOf(antes, antes.length);
        criarAlgSerial(alg).sort(depois);
        previewPanel.atualizar(antes, depois, alg + " - " + tipo);
    }

    private void imprimirResultados(List<ResultadoBenchmark> res) {
        if (res.isEmpty()) return;
        for (ResultadoBenchmark r : res) {
            resultadoArea.append(String.format("  Amostra %2d | %8.3f ms | Score: %6.0f | %s\n",
                    r.getAmostra(), r.getTempoMs(), r.getScore(),
                    r.isOrdenadoCorretamente() ? "OK" : "ERRO"));
        }
        double media  = BenchmarkStats.calcularMedia(res);
        double menor  = BenchmarkStats.calcularMenorTempo(res);
        double maior  = BenchmarkStats.calcularMaiorTempo(res);
        boolean todos = BenchmarkStats.todosOrdenadosCorretamente(res);
        resultadoArea.append("---------------------------------------\n");
        resultadoArea.append(String.format("  Media  : %.4f ms\n", media));
        resultadoArea.append(String.format("  Menor  : %.4f ms\n", menor));
        resultadoArea.append(String.format("  Maior  : %.4f ms\n", maior));
        resultadoArea.append("  Correto: " + (todos ? "Sim" : "NAO") + "\n");

        ResultadoBenchmark p = res.get(0);
        if (p.getVersao().equalsIgnoreCase("Serial")) {
            String ch = criarChave(p.getAlgoritmo(), p.getTamanhoEntrada(), TipoEntrada.valueOf(p.getTipoEntrada()));
            mediasSeriais.put(ch, media);
            resultadoArea.append("  Referencia serial registrada.\n");
        } else {
            String ch = criarChave(p.getAlgoritmo(), p.getTamanhoEntrada(), TipoEntrada.valueOf(p.getTipoEntrada()));
            double ref = mediasSeriais.getOrDefault(ch, 0.0);
            if (ref > 0) {
                resultadoArea.append(String.format("  Speedup    : %.3fx\n", ref / media));
                resultadoArea.append(String.format("  Eficiencia : %.3f\n",  (ref / media) / p.getThreads()));
            } else {
                resultadoArea.append("  Speedup: execute a versao serial primeiro.\n");
            }
        }
    }

    private void salvarCsv(List<ResultadoBenchmark> res) {
        CsvExporter.appendResults(arquivoCsvBruto, res);
        if (res.isEmpty()) return;
        ResultadoBenchmark p = res.get(0);
        String ch = criarChave(p.getAlgoritmo(), p.getTamanhoEntrada(), TipoEntrada.valueOf(p.getTipoEntrada()));
        double media = BenchmarkStats.calcularMedia(res);
        if (p.getVersao().equalsIgnoreCase("Serial")) {
            mediasSeriais.put(ch, media);
            CsvResumoExporter.appendResumo(arquivoCsvResumo, res, media);
        } else {
            CsvResumoExporter.appendResumo(arquivoCsvResumo, res, mediasSeriais.getOrDefault(ch, 0.0));
        }
    }

    private String criarChave(String a, int t, TipoEntrada tp) { return a + "|" + t + "|" + tp.name(); }

    private SortAlgorithm criarAlgSerial(String n) {
        switch (n) {
            case "Bubble Sort":    return new BubbleSort();
            case "Insertion Sort": return new InsertionSort();
            case "Quick Sort":     return new QuickSort();
            default:               return new MergeSort();
        }
    }
    private ParallelSortAlgorithm criarAlgParalelo(String n) {
        switch (n) {
            case "Bubble Sort":    return new ParallelBubbleSort();
            case "Insertion Sort": return new ParallelInsertionSort();
            case "Quick Sort":     return new ParallelQuickSort();
            default:               return new ParallelMergeSort();
        }
    }

    private JPanel makeCard() {
        return UiTheme.makeRoundPanel(UiTheme.PANEL, UiTheme.RADIUS);
    }
    private JLabel makeHeader(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Poppins", Font.BOLD, 20));
        l.setForeground(UiTheme.TEXT);
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }
    private JLabel makeFieldLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(UiTheme.SMALL_FONT);
        l.setForeground(UiTheme.TEXT_MUTED);
        return l;
    }
    private GridBagConstraints gbc(int x, int y, Insets ins) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y;
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        g.anchor = GridBagConstraints.WEST;
        g.insets = ins;
        return g;
    }
}
