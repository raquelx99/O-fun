package ui;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import javax.swing.table.DefaultTableModel;

public class ResultsPanel extends JPanel {

    private final MainWindow mainWindow;

    private JTable              tabela;
    private DefaultTableModel   tabelaModel;
    private BarChartPanel chartPanel;

    private JComboBox<String> graficoCombo;
    private JComboBox<String> algoritmoCombo;
    private JComboBox<String> tipoEntradaCombo;
    private JComboBox<String> tamanhoCombo;
    private JComboBox<String> versaoCombo;
    private JTextArea         analiseArea;

    private List<ResumoResultado> resultadosFiltradosAtuais;
    private List<ResumoResultado> resultados;

    private final String arquivoCsvResumo = "resultados_resumo_ui.csv";

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

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(sub);

        JButton atualizar = new JButton("Atualizar");
        UiTheme.stylePrimaryButton(atualizar);
        atualizar.addActionListener(e -> carregarResultados());

        topo.add(voltar,    BorderLayout.WEST);
        topo.add(textos,    BorderLayout.CENTER);
        topo.add(atualizar, BorderLayout.EAST);
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

        chartPanel = new BarChartPanel();
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

        JSplitPane splitDireita = new JSplitPane(JSplitPane.VERTICAL_SPLIT, wrapChart, analiseScroll);
        splitDireita.setResizeWeight(0.65);
        splitDireita.setDividerSize(5);
        splitDireita.setBorder(null);
        splitDireita.setOpaque(false);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabelaScroll, splitDireita);
        split.setResizeWeight(0.5);
        split.setDividerSize(6);
        split.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        split.setBackground(UiTheme.BG);
        split.setOpaque(false);
        return split;
    }

    private JPanel criarFiltros() {
        graficoCombo     = new JComboBox<>(new String[]{
                "Tempo medio por algoritmo","Serial vs Paralelo",
                "Speedup por threads","Eficiencia por threads",
                "Tempo por tamanho","Tempo por tipo de entrada"});
        algoritmoCombo   = new JComboBox<>(new String[]{"Todos","Bubble Sort","Insertion Sort","Quick Sort","Merge Sort"});
        tipoEntradaCombo = new JComboBox<>(new String[]{"Todos","ALEATORIA","ORDENADA","QUASE_ORDENADA","INVERTIDA","REPETIDA"});
        tamanhoCombo     = new JComboBox<>(new String[]{"Todos","1000","5000","10000","50000"});
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
            lbl.setFont(UiTheme.SMALL_FONT);
            lbl.setForeground(UiTheme.TEXT_MUTED);
            gc.gridx = i; gc.gridy = 0; gc.insets = new Insets(2, 6, 2, 6);
            linhaFiltros.add(lbl, gc);
            gc.gridy = 1;
            linhaFiltros.add(combos[i], gc);
        }

        JButton aplicar  = new JButton("Aplicar filtros");
        JButton analise  = new JButton("Gerar analise");
        UiTheme.stylePrimaryButton(aplicar);
        UiTheme.styleSecondaryButton(analise);
        aplicar.addActionListener(e -> atualizarTabelaEGrafico());
        analise.addActionListener(e -> gerarAnaliseTextual());

        JPanel botoes = new JPanel(new GridBagLayout());
        botoes.setOpaque(false);
        GridBagConstraints bg = new GridBagConstraints();
        bg.fill = GridBagConstraints.HORIZONTAL; bg.weightx = 1; bg.insets = new Insets(4, 4, 4, 4);
        bg.gridx = 0; bg.gridy = 0; botoes.add(aplicar, bg);
        bg.gridy = 1;               botoes.add(analise, bg);

        JPanel container = new JPanel(new BorderLayout(8, 0));
        container.setBackground(UiTheme.PANEL);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        container.add(linhaFiltros, BorderLayout.CENTER);
        container.add(botoes,       BorderLayout.EAST);
        return container;
    }

    private void gerarAnaliseTextual() {
        if (resultadosFiltradosAtuais == null || resultadosFiltradosAtuais.isEmpty()) {
            analiseArea.setText("Nao ha dados filtrados para analisar.");
            return;
        }
        analiseArea.setText(AnalysisTextGenerator.gerarAnalise(
                (String) graficoCombo.getSelectedItem(), resultadosFiltradosAtuais));
    }

    private void carregarResultados() {
        resultados = ResumoCsvReader.ler(arquivoCsvResumo);
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
        for (ResumoResultado r : f) {
            tabelaModel.addRow(new Object[]{
                r.getAlgoritmo(), r.getVersao(), r.getThreads(),
                r.getTamanhoEntrada(), r.getTipoEntrada(),
                fmt(r.getMediaMs()), fmt(r.getMenorMs()), fmt(r.getMaiorMs()),
                fmt(r.getSpeedup()), fmt(r.getEficiencia()),
                r.isTodosOrdenadosCorretamente() ? "OK" : "ERRO"
            });
        }
    }

    private void atualizarGrafico(List<ResumoResultado> f) {
        String tipo = (String) graficoCombo.getSelectedItem();
        Map<String, Double> dados; String titulo, eixo;
        switch (tipo) {
            case "Serial vs Paralelo":
                dados = agrupar(f, r -> r.getVersao().equals("Serial") ? "Serial" : "Paralelo "+r.getThreads()+"T", r -> r.getMediaMs());
                titulo = "Serial vs Paralelo"; eixo = "Tempo medio (ms)"; break;
            case "Speedup por threads":
                dados = agrupar(f.stream().filter(r -> r.getVersao().equals("Paralelo")).toList(),
                        r -> r.getThreads()+" threads", r -> r.getSpeedup());
                titulo = "Speedup por threads"; eixo = "Speedup"; break;
            case "Eficiencia por threads":
                dados = agrupar(f.stream().filter(r -> r.getVersao().equals("Paralelo")).toList(),
                        r -> r.getThreads()+" threads", r -> r.getEficiencia());
                titulo = "Eficiencia por threads"; eixo = "Eficiencia"; break;
            case "Tempo por tamanho":
                dados = agrupar(f, r -> String.valueOf(r.getTamanhoEntrada()), r -> r.getMediaMs());
                titulo = "Tempo por tamanho"; eixo = "Tempo medio (ms)"; break;
            case "Tempo por tipo de entrada":
                dados = agrupar(f, r -> r.getTipoEntrada(), r -> r.getMediaMs());
                titulo = "Tempo por tipo de entrada"; eixo = "Tempo medio (ms)"; break;
            default:
                dados = agrupar(f, r -> r.getAlgoritmo(), r -> r.getMediaMs());
                titulo = "Tempo medio por algoritmo"; eixo = "Tempo (ms)"; break;
        }
        chartPanel.setDados(dados, titulo, eixo);
    }

    @FunctionalInterface interface KF { String k(ResumoResultado r); }
    @FunctionalInterface interface VF { double v(ResumoResultado r); }

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

    private String fmt(double v) { return String.format("%.4f", v); }
}
