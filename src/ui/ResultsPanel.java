package ui;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import benchmark.AmbienteExecucaoLogger;
import benchmark.BenchmarkConfig;
import benchmark.BenchmarkRunner;
import benchmark.BenchmarkStats;
import benchmark.CsvExporter;
import benchmark.CsvResumoExporter;
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

public class ResultsPanel extends JPanel {

    private final MainWindow mainWindow;

    private JTable            tabela;
    private DefaultTableModel tabelaModel;
    private ChartPanel        chartPanel;

    private JComboBox<String> graficoCombo;
    private JComboBox<String> algoritmoCombo;
    private JComboBox<String> tipoEntradaCombo;
    private JComboBox<String> tamanhoCombo;
    private JComboBox<String> versaoCombo;
    private JTextArea         analiseArea;

    private List<ResumoResultado> resultadosFiltradosAtuais = new ArrayList<>();
    private List<ResumoResultado> resultados                = new ArrayList<>();

    private final String arquivoCsvBruto  = "resultados_sorts.csv";
    private final String arquivoCsvResumo = "resultados_resumo.csv";
    private final String arquivoAmbiente  = "ambiente_execucao.txt";

    private static final int[]         TAMANHOS = {1_000, 5_000, 10_000, 100_000};
    private static final int[]         THREADS  = {1, 2, 4, 8};
    private static final int           AMOSTRAS = 5;
    private static final TipoEntrada[] TIPOS    = {
        TipoEntrada.ALEATORIA, TipoEntrada.ORDENADA,
        TipoEntrada.QUASE_ORDENADA, TipoEntrada.INVERTIDA, TipoEntrada.REPETIDA
    };

    private static final int TOTAL = TIPOS.length * TAMANHOS.length * (4 + 4 * THREADS.length);

    private static final String[] TIPOS_GRAFICO = {
        "Tempo medio por algoritmo",
        "Serial vs Paralelo",
        "Speedup por threads",
        "Eficiencia por threads",
        "Tempo por tamanho",
        "Tempo por tipo de entrada",
        "Speedup por algoritmo (agrupado)",
        "Eficiencia por algoritmo (agrupado)",
        "Tempo por tipo de entrada por algoritmo (agrupado)",
        "Curva de crescimento — todos algoritmos (linhas)",
        "Serial vs Paralelo 8T por algoritmo (linhas)",
    };

    public ResultsPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout(0, 0));
        setBackground(UiTheme.BG);
        add(criarTopo(),    BorderLayout.NORTH);
        add(criarCorpo(),   BorderLayout.CENTER);
        add(criarFiltros(), BorderLayout.SOUTH);
        carregarResultados();
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
        JLabel titulo = new JLabel("Resultados e Graficos");
        titulo.setFont(new Font("Poppins", Font.BOLD, 22));
        titulo.setForeground(UiTheme.TEXT);
        titulo.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sub = new JLabel("Analise estatistica dos benchmarks executados");
        sub.setFont(UiTheme.SMALL_FONT);
        sub.setForeground(UiTheme.TEXT_MUTED);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        textos.add(titulo); textos.add(Box.createVerticalStrut(2)); textos.add(sub);

        JPanel painelDireito = new JPanel(new GridBagLayout());
        painelDireito.setOpaque(false);
        GridBagConstraints bg = new GridBagConstraints();
        bg.fill = GridBagConstraints.HORIZONTAL; bg.weightx = 1;
        bg.insets = new Insets(2, 4, 2, 4);

        JButton gerarBtn = new JButton("Gerar dados do trabalho");
        UiTheme.styleGoldButton(gerarBtn);
        gerarBtn.setToolTipText("Executa " + TOTAL + " benchmarks (inclui 100k de elementos)");
        gerarBtn.addActionListener(e -> confirmarEGerarDados(gerarBtn));

        JButton atualizar = new JButton("Atualizar tabela");
        UiTheme.stylePrimaryButton(atualizar);
        atualizar.addActionListener(e -> carregarResultados());

        bg.gridx = 0; bg.gridy = 0; painelDireito.add(gerarBtn,  bg);
        bg.gridy = 1;               painelDireito.add(atualizar, bg);

        topo.add(voltar,        BorderLayout.WEST);
        topo.add(textos,        BorderLayout.CENTER);
        topo.add(painelDireito, BorderLayout.EAST);
        return topo;
    }

    private JSplitPane criarCorpo() {
        String[] cols = {"Algoritmo","Versao","Threads","Tamanho","Tipo",
                "Media ms","Menor ms","Maior ms","Speedup","Eficiencia","OK"};
        tabelaModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabela = new JTable(tabelaModel);
        UiTheme.styleTable(tabela);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane tabelaScroll = new JScrollPane(tabela);
        UiTheme.styleScrollPane(tabelaScroll);
        tabelaScroll.setBorder(UiTheme.createSectionBorder("Tabela de Resultados"));

        chartPanel = new ChartPanel();
        chartPanel.setMinimumSize(new Dimension(0, 180));
        JPanel wrapChart = UiTheme.makeRoundPanel(UiTheme.PANEL_DARK, UiTheme.RADIUS);
        wrapChart.setLayout(new BorderLayout());
        wrapChart.setBorder(UiTheme.createSectionBorder("Grafico"));
        wrapChart.add(chartPanel, BorderLayout.CENTER);

        analiseArea = new JTextArea();
        analiseArea.setEditable(false);
        UiTheme.styleTextArea(analiseArea);
        analiseArea.setFont(new Font("Poppins", Font.PLAIN, 13));
        JScrollPane analiseScroll = new JScrollPane(analiseArea);
        UiTheme.styleScrollPane(analiseScroll);
        analiseScroll.setBorder(UiTheme.createSectionBorder("Analise Textual"));
        analiseScroll.setMinimumSize(new Dimension(0, 70));

        JSplitPane splitDir = new JSplitPane(JSplitPane.VERTICAL_SPLIT, wrapChart, analiseScroll);
        splitDir.setResizeWeight(0.65); splitDir.setDividerSize(5); splitDir.setBorder(null);
        splitDir.setOpaque(false);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabelaScroll, splitDir);
        split.setResizeWeight(0.5); split.setDividerSize(6);
        split.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        split.setBackground(UiTheme.BG); split.setOpaque(false);
        return split;
    }

    private JPanel criarFiltros() {
        graficoCombo     = new JComboBox<>(TIPOS_GRAFICO);
        algoritmoCombo   = new JComboBox<>(new String[]{"Todos","Bubble Sort","Insertion Sort","Quick Sort","Merge Sort"});
        tipoEntradaCombo = new JComboBox<>(new String[]{"Todos","ALEATORIA","ORDENADA","QUASE_ORDENADA","INVERTIDA","REPETIDA"});
        tamanhoCombo     = new JComboBox<>(new String[]{"Todos","1000","5000","10000","100000"});
        versaoCombo      = new JComboBox<>(new String[]{"Todos","Serial","Paralelo"});

        for (JComboBox<?> c : new JComboBox[]{graficoCombo,algoritmoCombo,tipoEntradaCombo,tamanhoCombo,versaoCombo})
            UiTheme.styleCombo(c);

        JPanel linhaFiltros = new JPanel(new GridBagLayout());
        linhaFiltros.setOpaque(false);
        String[] labels = {"Grafico:","Algoritmo:","Tipo entrada:","Tamanho:","Versao:"};
        JComboBox<?>[] combos = {graficoCombo,algoritmoCombo,tipoEntradaCombo,tamanhoCombo,versaoCombo};
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        for (int i = 0; i < combos.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(UiTheme.SMALL_FONT); lbl.setForeground(UiTheme.TEXT_MUTED);
            gc.gridx = i; gc.gridy = 0; gc.insets = new Insets(2,6,2,6);
            linhaFiltros.add(lbl, gc);
            gc.gridy = 1; linhaFiltros.add(combos[i], gc);
        }

        JButton aplicar = new JButton("Aplicar filtros");
        JButton analise = new JButton("Gerar analise");
        UiTheme.stylePrimaryButton(aplicar);
        UiTheme.styleSecondaryButton(analise);
        aplicar.addActionListener(e -> atualizarTabelaEGrafico());
        analise.addActionListener(e -> gerarAnaliseTextual());

        JPanel botoes = new JPanel(new GridBagLayout());
        botoes.setOpaque(false);
        GridBagConstraints bg = new GridBagConstraints();
        bg.fill = GridBagConstraints.HORIZONTAL; bg.weightx = 1; bg.insets = new Insets(4,4,4,4);
        bg.gridx = 0; bg.gridy = 0; botoes.add(aplicar, bg);
        bg.gridy = 1;               botoes.add(analise, bg);

        JPanel container = new JPanel(new BorderLayout(8, 0));
        container.setBackground(UiTheme.PANEL);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1,0,0,0,UiTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10,14,10,14)));
        container.add(linhaFiltros, BorderLayout.CENTER);
        container.add(botoes,       BorderLayout.EAST);
        return container;
    }

    private void confirmarEGerarDados(JButton gerarBtn) {
        int resp = JOptionPane.showConfirmDialog(this,
            "<html><b>Gerar dados completos do trabalho?</b><br><br>" +
            "Executa <b>" + TOTAL + " benchmarks</b>:<br>" +
            "  - 4 algoritmos x 2 versoes x 4 configs de thread<br>" +
            "  - 5 tamanhos: 1k, 5k, 10k, <b>100k</b><br>" +
            "  - 5 tipos de entrada, 5 amostras cada<br><br>" +
            "<b>Atencao:</b> 100k pode levar varios minutos para Bubble Sort e Insertion Sort.<br>" +
            "Os CSVs existentes serao sobrescritos.</html>",
            "Confirmar geracao", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resp != JOptionPane.OK_OPTION) return;
        executarBenchmarkCompleto(gerarBtn);
    }

    private void executarBenchmarkCompleto(JButton gerarBtn) {
        JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                "Gerando dados...", false);
        dialog.setSize(540, 270);
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel dp = new JPanel(new BorderLayout(0,8));
        dp.setBackground(UiTheme.PANEL);
        dp.setBorder(BorderFactory.createEmptyBorder(18,20,18,20));

        JLabel statusLbl = new JLabel("Inicializando...");
        statusLbl.setFont(new Font("Poppins", Font.BOLD, 13));
        statusLbl.setForeground(UiTheme.TEXT);

        JProgressBar barra = new JProgressBar(0, TOTAL);
        barra.setStringPainted(true);
        barra.setString("0 / " + TOTAL);
        barra.setForeground(UiTheme.BLUE);
        barra.setBackground(UiTheme.PANEL_DARK);
        barra.setPreferredSize(new Dimension(0, 22));

        JTextArea logArea = new JTextArea(7, 0);
        logArea.setEditable(false);
        UiTheme.styleTextArea(logArea);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(logArea);
        UiTheme.styleScrollPane(logScroll);

        JLabel etaLbl = new JLabel(" ");
        etaLbl.setFont(UiTheme.SMALL_FONT);
        etaLbl.setForeground(UiTheme.TEXT_MUTED);

        JPanel sul = new JPanel(new BorderLayout(0,4));
        sul.setOpaque(false);
        sul.add(etaLbl, BorderLayout.NORTH);
        sul.add(logScroll, BorderLayout.CENTER);

        dp.add(statusLbl, BorderLayout.NORTH);
        dp.add(barra,     BorderLayout.CENTER);
        dp.add(sul,       BorderLayout.SOUTH);
        dialog.setContentPane(dp);
        dialog.setVisible(true);

        gerarBtn.setEnabled(false);
        long[] inicioTotal = {System.currentTimeMillis()};

        SwingWorker<Void, String[]> worker = new SwingWorker<>() {
            int execucaoAtual = 0;

            @Override
            protected Void doInBackground() throws Exception {
                CsvExporter.writeHeader(arquivoCsvBruto);
                CsvResumoExporter.writeHeader(arquivoCsvResumo);
                AmbienteExecucaoLogger.salvar(arquivoAmbiente);
                BenchmarkRunner runner = new BenchmarkRunner();
                Map<String, Double> mediasSeriais = new HashMap<>();

                List<SortAlgorithm> seriais = List.of(
                    new BubbleSort(), new InsertionSort(), new QuickSort(), new MergeSort());
                List<ParallelSortAlgorithm> paralelos = List.of(
                    new ParallelBubbleSort(), new ParallelInsertionSort(),
                    new ParallelQuickSort(), new ParallelMergeSort());

                for (TipoEntrada tipo : TIPOS) {
                    for (int tamanho : TAMANHOS) {
                        boolean gigante = tamanho >= 1_000_000;
                        BenchmarkConfig config = new BenchmarkConfig(tamanho, tipo, AMOSTRAS);

                        for (SortAlgorithm alg : seriais) {
                            if (isCancelled()) return null;
                            if (gigante && (alg instanceof BubbleSort || alg instanceof InsertionSort)) {
                                execucaoAtual++;
                                publish(new String[]{"Pulado (muito lento): " + alg.getName() + " serial 1M", null, calcEta(inicioTotal[0], execucaoAtual)});
                                continue;
                            }
                            publish(new String[]{"Serial: " + alg.getName() + " | " + tamanho + " | " + tipo, null, null});
                            List<ResultadoBenchmark> res = runner.runSerial(alg, config);
                            double media = BenchmarkStats.calcularMedia(res);
                            mediasSeriais.put(alg.getName() + "|" + tamanho + "|" + tipo.name(), media);
                            CsvExporter.appendResults(arquivoCsvBruto, res);
                            CsvResumoExporter.appendResumo(arquivoCsvResumo, res, media);
                            execucaoAtual++;
                            publish(new String[]{null, "  OK  " + String.format("%.3f ms", media), calcEta(inicioTotal[0], execucaoAtual)});
                        }

                        for (ParallelSortAlgorithm alg : paralelos) {
                            for (int threads : THREADS) {
                                if (isCancelled()) return null;
                                if (gigante && threads == 1 &&
                                    (alg instanceof ParallelBubbleSort || alg instanceof ParallelInsertionSort)) {
                                    execucaoAtual++;
                                    publish(new String[]{"Pulado: " + alg.getName() + " paralelo 1T 1M", null, calcEta(inicioTotal[0], execucaoAtual)});
                                    continue;
                                }
                                publish(new String[]{"Paralelo: " + alg.getName() + " " + threads + "T | " + tamanho + " | " + tipo, null, null});
                                List<ResultadoBenchmark> res = runner.runParallel(alg, config, threads);
                                double media = BenchmarkStats.calcularMedia(res);
                                double ref   = mediasSeriais.getOrDefault(alg.getName() + "|" + tamanho + "|" + tipo.name(), 0.0);
                                CsvExporter.appendResults(arquivoCsvBruto, res);
                                CsvResumoExporter.appendResumo(arquivoCsvResumo, res, ref);
                                execucaoAtual++;
                                String sp = ref > 0 ? String.format("speedup %.2fx", ref / media) : "-";
                                publish(new String[]{null, "  OK  " + String.format("%.3f ms", media) + " | " + sp, calcEta(inicioTotal[0], execucaoAtual)});
                            }
                        }
                    }
                }
                return null;
            }

            @Override protected void process(List<String[]> chunks) {
                for (String[] msg : chunks) {
                    if (msg[0] != null) { statusLbl.setText(msg[0]); barra.setValue(execucaoAtual); barra.setString(execucaoAtual + " / " + TOTAL); logArea.append(msg[0] + "\n"); }
                    if (msg[1] != null) { logArea.append(msg[1] + "\n"); logArea.setCaretPosition(logArea.getDocument().getLength()); }
                    if (msg[2] != null) { etaLbl.setText(msg[2]); }
                }
            }

            @Override protected void done() {
                gerarBtn.setEnabled(true);
                dialog.dispose();
                try {
                    get();
                    long totalSeg = (System.currentTimeMillis() - inicioTotal[0]) / 1000;
                    JOptionPane.showMessageDialog(ResultsPanel.this,
                        "<html><b>Dados gerados!</b><br>Tempo total: " + totalSeg + "s<br>" +
                        "CSV: " + arquivoCsvResumo + "</html>",
                        "Concluido!", JOptionPane.INFORMATION_MESSAGE);
                    carregarResultados();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ResultsPanel.this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private String calcEta(long inicio, int feitos) {
        if (feitos == 0) return " ";
        long dec = System.currentTimeMillis() - inicio;
        long eta = feitos < TOTAL ? (dec / feitos) * (TOTAL - feitos) : 0;
        return String.format("Progresso: %d/%d  |  Decorrido: %ds  |  Estimativa: %ds", feitos, TOTAL, dec/1000, eta/1000);
    }

    private void gerarAnaliseTextual() {
        if (resultadosFiltradosAtuais.isEmpty()) { analiseArea.setText("Sem dados para analisar."); return; }
        analiseArea.setText(AnalysisTextGenerator.gerarAnalise(
                (String) graficoCombo.getSelectedItem(), resultadosFiltradosAtuais));
    }

    private void carregarResultados() {
        resultados = ResumoCsvReader.ler(arquivoCsvResumo);
        if (resultados.isEmpty()) resultados = ResumoCsvReader.ler("resultados_resumo_ui.csv");
        atualizarTabelaEGrafico();
    }

    private void atualizarTabelaEGrafico() {
        resultadosFiltradosAtuais = filtrarResultados();
        atualizarTabela(resultadosFiltradosAtuais);
        atualizarGrafico(resultadosFiltradosAtuais);
        analiseArea.setText("Clique em \"Gerar analise\" para interpretar os dados filtrados.");
    }

    private List<ResumoResultado> filtrarResultados() {
        String alg    = (String) algoritmoCombo.getSelectedItem();
        String tipo   = (String) tipoEntradaCombo.getSelectedItem();
        String tam    = (String) tamanhoCombo.getSelectedItem();
        String versao = (String) versaoCombo.getSelectedItem();
        return resultados.stream()
                .filter(r -> alg.equals("Todos")    || r.getAlgoritmo().equals(alg))
                .filter(r -> tipo.equals("Todos")   || r.getTipoEntrada().equals(tipo))
                .filter(r -> tam.equals("Todos")    || String.valueOf(r.getTamanhoEntrada()).equals(tam))
                .filter(r -> versao.equals("Todos") || r.getVersao().equals(versao))
                .toList();
    }

    private void atualizarTabela(List<ResumoResultado> f) {
        tabelaModel.setRowCount(0);
        for (ResumoResultado r : f)
            tabelaModel.addRow(new Object[]{
                r.getAlgoritmo(), r.getVersao(), r.getThreads(),
                r.getTamanhoEntrada(), r.getTipoEntrada(),
                fmt(r.getMediaMs()), fmt(r.getMenorMs()), fmt(r.getMaiorMs()),
                fmt(r.getSpeedup()), fmt(r.getEficiencia()),
                r.isTodosOrdenadosCorretamente() ? "OK" : "ERRO"
            });
    }

    private void atualizarGrafico(List<ResumoResultado> f) {
        String tipo = (String) graficoCombo.getSelectedItem();
        switch (tipo) {
            case "Tempo medio por algoritmo" ->
                chartPanel.setDados(agrupar(f, r -> r.getAlgoritmo(), r -> r.getMediaMs()),
                    "Tempo medio por algoritmo", "Tempo (ms)");
            case "Serial vs Paralelo" ->
                chartPanel.setDados(agrupar(f,
                    r -> r.getVersao().equals("Serial") ? "Serial" : "Paralelo "+r.getThreads()+"T",
                    r -> r.getMediaMs()), "Serial vs Paralelo", "Tempo medio (ms)");
            case "Speedup por threads" ->
                chartPanel.setDados(agrupar(
                    f.stream().filter(r -> r.getVersao().equals("Paralelo")).toList(),
                    r -> r.getThreads()+" threads", r -> r.getSpeedup()),
                    "Speedup por threads", "Speedup");
            case "Eficiencia por threads" ->
                chartPanel.setDados(agrupar(
                    f.stream().filter(r -> r.getVersao().equals("Paralelo")).toList(),
                    r -> r.getThreads()+" threads", r -> r.getEficiencia()),
                    "Eficiencia por threads", "Eficiencia");
            case "Tempo por tamanho" ->
                chartPanel.setDados(agrupar(f,
                    r -> formatarTamanho(r.getTamanhoEntrada()), r -> r.getMediaMs()),
                    "Tempo por tamanho da entrada", "Tempo medio (ms)");
            case "Tempo por tipo de entrada" ->
                chartPanel.setDados(agrupar(f, r -> r.getTipoEntrada(), r -> r.getMediaMs()),
                    "Tempo por tipo de entrada", "Tempo medio (ms)");

            case "Speedup por algoritmo (agrupado)" ->
                graficoBarrasAgrupadas(
                    f.stream().filter(r -> r.getVersao().equals("Paralelo")).toList(),
                    r -> r.getAlgoritmo(), r -> r.getThreads()+" threads", r -> r.getSpeedup(),
                    "Speedup por algoritmo e quantidade de threads", "Speedup");
            case "Eficiencia por algoritmo (agrupado)" ->
                graficoBarrasAgrupadas(
                    f.stream().filter(r -> r.getVersao().equals("Paralelo")).toList(),
                    r -> r.getAlgoritmo(), r -> r.getThreads()+" threads", r -> r.getEficiencia(),
                    "Eficiencia paralela por algoritmo e threads", "Eficiencia");
            case "Tempo por tipo de entrada por algoritmo (agrupado)" ->
                graficoBarrasAgrupadas(f,
                    r -> r.getTipoEntrada(), r -> r.getAlgoritmo(), r -> r.getMediaMs(),
                    "Tempo por tipo de entrada e algoritmo", "Tempo medio (ms)");

            case "Curva de crescimento — todos algoritmos (linhas)" ->
                graficoLinhasCrescimento(f);
            case "Serial vs Paralelo 8T por algoritmo (linhas)" ->
                graficoLinhasSerialVsParalelo(f);
        }
    }

    @FunctionalInterface interface KF { String k(ResumoResultado r); }
    @FunctionalInterface interface VF { double v(ResumoResultado r); }

    private void graficoBarrasAgrupadas(List<ResumoResultado> lista,
                                         KF catFn, KF grupoFn, VF valFn,
                                         String titulo, String eixoY) {

        List<String> categorias = new ArrayList<>();
        List<String> grupos     = new ArrayList<>();
        Map<String, Map<String, List<Double>>> acum = new LinkedHashMap<>();

        for (ResumoResultado r : lista) {
            String cat   = catFn.k(r);
            String grupo = grupoFn.k(r);
            if (!categorias.contains(cat))   categorias.add(cat);
            if (!grupos.contains(grupo))     grupos.add(grupo);
            acum.computeIfAbsent(cat, k -> new LinkedHashMap<>())
                .computeIfAbsent(grupo, k -> new ArrayList<>())
                .add(valFn.v(r));
        }

        grupos.sort((a, b) -> {
            try { return Integer.compare(Integer.parseInt(a.replaceAll("[^0-9]", "")),
                                         Integer.parseInt(b.replaceAll("[^0-9]", ""))); }
            catch (Exception e) { return a.compareTo(b); }
        });

        Map<String, Map<String, Double>> dados = new LinkedHashMap<>();
        for (String cat : categorias) {
            Map<String, Double> gMap = new LinkedHashMap<>();
            for (String grupo : grupos) {
                List<Double> vals = acum.getOrDefault(cat, Map.of()).getOrDefault(grupo, List.of());
                if (!vals.isEmpty()) gMap.put(grupo, vals.stream().mapToDouble(d->d).average().orElse(0));
            }
            dados.put(cat, gMap);
        }

        chartPanel.setDadosAgrupados(dados, grupos, titulo, eixoY);
    }

    private void graficoLinhasCrescimento(List<ResumoResultado> f) {
        Map<String, List<double[]>> series = new LinkedHashMap<>();
        Map<String, Map<Integer, List<Double>>> acum = new LinkedHashMap<>();

        for (ResumoResultado r : f) {
            acum.computeIfAbsent(r.getAlgoritmo(), k -> new TreeMap<>())
                .computeIfAbsent(r.getTamanhoEntrada(), k -> new ArrayList<>())
                .add(r.getMediaMs());
        }

        for (String alg : List.of("Bubble Sort","Insertion Sort","Quick Sort","Merge Sort")) {
            if (!acum.containsKey(alg)) continue;
            List<double[]> pts = new ArrayList<>();
            for (Map.Entry<Integer, List<Double>> e : acum.get(alg).entrySet()) {
                double media = e.getValue().stream().mapToDouble(d->d).average().orElse(0);
                pts.add(new double[]{e.getKey(), media});
            }
            series.put(alg, pts);
        }

        chartPanel.setDadosLinhas(series,
            "Curva de crescimento — Tempo medio por tamanho de entrada",
            "Tempo medio (ms)");
    }

    private void graficoLinhasSerialVsParalelo(List<ResumoResultado> f) {
        Map<String, List<double[]>> series = new LinkedHashMap<>();

        for (String alg : List.of("Bubble Sort","Insertion Sort","Quick Sort","Merge Sort")) {
            for (String ver : List.of("Serial", "Paralelo 8T")) {
                String serie = alg.replace(" Sort","") + " " + ver;
                List<double[]> pts = new ArrayList<>();

                for (ResumoResultado r : f) {
                    if (!r.getAlgoritmo().equals(alg)) continue;
                    boolean isSerial   = ver.equals("Serial") && r.getVersao().equals("Serial");
                    boolean isParalelo = ver.equals("Paralelo 8T") && r.getVersao().equals("Paralelo") && r.getThreads() == 8;
                    if (isSerial || isParalelo)
                        pts.add(new double[]{r.getTamanhoEntrada(), r.getMediaMs()});
                }

                if (!pts.isEmpty()) series.put(serie, pts);
            }
        }

        chartPanel.setDadosLinhas(series,
            "Serial vs Paralelo 8 threads por algoritmo",
            "Tempo medio (ms)");
    }

    private Map<String, Double> agrupar(List<ResumoResultado> lista, KF kf, VF vf) {
        Map<String, Double>  soma = new LinkedHashMap<>();
        Map<String, Integer> cnt  = new LinkedHashMap<>();
        for (ResumoResultado r : lista) {
            String k = kf.k(r);
            soma.put(k, soma.getOrDefault(k, 0.0) + vf.v(r));
            cnt.put(k,  cnt.getOrDefault(k, 0) + 1);
        }
        Map<String, Double> med = new LinkedHashMap<>();
        soma.forEach((k, v) -> med.put(k, v / cnt.getOrDefault(k, 1)));
        return med;
    }

    private String formatarTamanho(int t) {
        return t >= 1_000_000 ? (t/1_000_000) + "M"
             : t >= 1_000     ? (t/1_000)     + "k"
             : String.valueOf(t);
    }

    private String fmt(double v) { return String.format("%.4f", v); }
}
