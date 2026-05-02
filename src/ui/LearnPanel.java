package ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

public class LearnPanel extends JPanel {

    private final MainWindow mainWindow;

    private int[]   valores;
    private Color[] coresBlocos;
    private static final int N = 8;

    private enum Modo { LIVRE, GUIADO }
    private Modo modo = Modo.LIVRE;

    private int dragging = -1, dragX, dragOffX, dropTarget = -1;

    private java.util.List<int[]> passos;
    private int     passoAtual = -1, highlightA = -1, highlightB = -1;
    private boolean isSwap = false;

    private boolean animandoSwap = false;
    private float   swapProgress = 0f;
    private int     swapIdxA, swapIdxB;
    private Timer   swapTimer;

    private boolean shakingA = false, shakingB = false;
    private float   shakePhase = 0f;
    private Timer   shakeTimer;

    private boolean pulsing = false;
    private float   pulsePhase = 0f;
    private Timer   pulseTimer;

    private float   dicaPhase = 0f;
    private Timer   dicaTimer;

    private String  bannerTxt   = null;
    private Color   bannerCor   = Color.WHITE;
    private float   bannerAlpha = 0f;
    private float   bannerY     = 0f;
    private Timer   bannerTimer;

    private int compA = -1;
    private boolean mostraSeta = false;

    private boolean aguardandoAcao = false;
    private int     acaoEsperadaA = -1, acaoEsperadaB = -1;
    private boolean acaoEhSwap = false;

    private int pontos = 0, erros = 0, acertosSeq = 0;
    private int comparacoes = 0, swaps = 0;

    private static final int BLOCK_W = 72, BLOCK_H = 72, BLOCK_GAP = 14, BOARD_Y = 60;

    private static final Color[] PALETA = {
        new Color(110,130,235), new Color(235,120,70),
        new Color(80,200,130),  new Color(160,90,230),
        new Color(70,190,210),  new Color(230,80,80),
        new Color(140,210,60),  new Color(220,130,200),
    };

    private JComboBox<String> algoCombo;
    private JButton modoBtn, passoBtn, autoBtn, embaralharBtn, resetBtn, okBtn;
    private JLabel  statusLabel, contadorLabel, pontosLabel;
    private InstructionBanner instructionBanner;
    private JTextArea explicacaoArea;
    private BoardPanel boardPanel;
    private ProgressBarPanel progressBar;
    private boolean autoRodando = false;
    private Timer autoTimer;

    public LearnPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout(0, 0));
        setBackground(UiTheme.BG);
        inicializarValores();
        add(criarTopo(),      BorderLayout.NORTH);
        add(criarCentro(),    BorderLayout.CENTER);
        add(criarControles(), BorderLayout.SOUTH);
    }

    private void inicializarValores() {
        valores = new int[N]; coresBlocos = new Color[N];
        Random rng = new Random(); Set<Integer> usados = new HashSet<>();
        for (int i = 0; i < N; i++) {
            int v; do { v = 1+rng.nextInt(20); } while (usados.contains(v));
            usados.add(v); valores[i] = v; coresBlocos[i] = PALETA[i%PALETA.length];
        }
        resetarEstado();
    }

    private void resetarEstado() {
        passos = null; passoAtual = -1;
        highlightA = -1; highlightB = -1; isSwap = false;
        comparacoes = 0; swaps = 0;
        compA = -1; mostraSeta = false;
        aguardandoAcao = false; acaoEsperadaA = -1; acaoEsperadaB = -1;
        if (modo == Modo.GUIADO) { pontos = 0; erros = 0; acertosSeq = 0; }
        pararDicaPiscante();
        atualizarUI();
    }

    private void embaralhar() {
        pararAuto();
        Random rng = new Random();
        for (int i = N-1; i > 0; i--) {
            int j = rng.nextInt(i+1);
            int tv = valores[i]; valores[i] = valores[j]; valores[j] = tv;
            Color tc = coresBlocos[i]; coresBlocos[i] = coresBlocos[j]; coresBlocos[j] = tc;
        }
        resetarEstado();
        if (instructionBanner != null) instructionBanner.limpar();
        setStatus(modo==Modo.LIVRE ? "Arraste para ordenar ou use os controles."
                                   : "Clique em 'Proximo passo' para comecar.");
        boardPanel.repaint();
    }

    private JPanel criarTopo() {
        JPanel topo = new JPanel(new BorderLayout(10, 0));
        topo.setOpaque(false);
        topo.setBorder(BorderFactory.createEmptyBorder(12,16,6,16));

        JButton voltar = new JButton("< Voltar");
        UiTheme.styleSecondaryButton(voltar);
        voltar.addActionListener(e -> mainWindow.mostrarMenuPrincipal());

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        JLabel titulo = new JLabel("Modo Aprenda");
        titulo.setFont(new Font("Poppins", Font.BOLD, 22));
        titulo.setForeground(UiTheme.TEXT);
        titulo.setAlignmentX(CENTER_ALIGNMENT);
        JLabel sub = new JLabel("Arraste livremente ou jogue no modo guiado");
        sub.setFont(UiTheme.SMALL_FONT);
        sub.setForeground(UiTheme.TEXT_MUTED);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        textos.add(titulo); textos.add(Box.createVerticalStrut(2)); textos.add(sub);

        modoBtn = new JButton("Modo: Livre");
        UiTheme.styleSecondaryButton(modoBtn);
        modoBtn.addActionListener(e -> alternarModo());

        topo.add(voltar, BorderLayout.WEST);
        topo.add(textos, BorderLayout.CENTER);
        topo.add(modoBtn, BorderLayout.EAST);
        return topo;
    }

    private JSplitPane criarCentro() {
        boardPanel   = new BoardPanel();
        progressBar  = new ProgressBarPanel();
        progressBar.setPreferredSize(new Dimension(0, 22));
        instructionBanner = new InstructionBanner();
        instructionBanner.setPreferredSize(new Dimension(0, 62));

        JPanel painelJogo = new JPanel(new BorderLayout(0, 4));
        painelJogo.setOpaque(false);
        painelJogo.add(instructionBanner, BorderLayout.NORTH);
        painelJogo.add(boardPanel,        BorderLayout.CENTER);
        painelJogo.add(progressBar,       BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelJogo, criarPainelDidatico());
        split.setResizeWeight(0.70);
        split.setDividerSize(5);
        split.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
        split.setBackground(UiTheme.BG);
        split.setOpaque(false);
        return split;
    }

    private JPanel criarPainelDidatico() {
        JPanel card = UiTheme.makeRoundPanel(UiTheme.PANEL, UiTheme.RADIUS);
        card.setLayout(new BorderLayout(0,8));
        card.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));

        JLabel titulo = new JLabel("O que esta acontecendo?");
        titulo.setFont(new Font("Poppins", Font.BOLD, 13));
        titulo.setForeground(UiTheme.GOLD);

        explicacaoArea = new JTextArea();
        explicacaoArea.setEditable(false);
        explicacaoArea.setLineWrap(true); explicacaoArea.setWrapStyleWord(true);
        explicacaoArea.setFont(new Font("Poppins", Font.PLAIN, 13));
        explicacaoArea.setBackground(UiTheme.PANEL_DARK);
        explicacaoArea.setForeground(UiTheme.TEXT);
        explicacaoArea.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        explicacaoArea.setText(explicacaoInicial());

        JScrollPane scroll = new JScrollPane(explicacaoArea);
        UiTheme.styleScrollPane(scroll);

        pontosLabel = new JLabel("Pontos: 0  |  Erros: 0");
        pontosLabel.setFont(new Font("Poppins", Font.BOLD, 12));
        pontosLabel.setForeground(UiTheme.BLUE_LIGHT);
        pontosLabel.setVisible(false);

        card.add(titulo, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(pontosLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel criarControles() {
        JPanel panel = new JPanel(new BorderLayout(12,0));
        panel.setBackground(UiTheme.PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,UiTheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10,16,10,16)));

        JPanel esq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        esq.setOpaque(false);

        JLabel algoLabel = new JLabel("Algoritmo:");
        algoLabel.setFont(UiTheme.SMALL_FONT);
        algoLabel.setForeground(UiTheme.TEXT_MUTED);

        algoCombo = new JComboBox<>(new String[]{"Bubble Sort","Selection Sort","Insertion Sort","Quick Sort","Merge Sort"});
        UiTheme.styleCombo(algoCombo);
        algoCombo.setPreferredSize(new Dimension(155,32));
        algoCombo.addActionListener(e -> { pararAuto(); resetarEstado(); explicacaoArea.setText(explicacaoInicial()); if (instructionBanner!=null) instructionBanner.limpar(); boardPanel.repaint(); });

        passoBtn = new JButton("Proximo passo");
        UiTheme.stylePrimaryButton(passoBtn);
        passoBtn.addActionListener(e -> proximoPasso());

        autoBtn = new JButton("Executar auto");
        UiTheme.styleSecondaryButton(autoBtn);
        autoBtn.addActionListener(e -> toggleAuto());

        okBtn = new JButton("OK — sem troca");
        UiTheme.styleGoldButton(okBtn);
        okBtn.setVisible(false);
        okBtn.addActionListener(e -> confirmarSemTroca());

        embaralharBtn = new JButton("Embaralhar");
        UiTheme.styleSecondaryButton(embaralharBtn);
        embaralharBtn.addActionListener(e -> embaralhar());

        resetBtn = new JButton("Resetar");
        UiTheme.styleSecondaryButton(resetBtn);
        resetBtn.addActionListener(e -> { pararAuto(); inicializarValores(); boardPanel.repaint(); setStatus("Novo conjunto gerado!"); explicacaoArea.setText(explicacaoInicial()); if (instructionBanner!=null) instructionBanner.limpar(); });

        esq.add(algoLabel); esq.add(algoCombo);
        esq.add(Box.createHorizontalStrut(4));
        esq.add(passoBtn); esq.add(autoBtn); esq.add(okBtn);
        esq.add(Box.createHorizontalStrut(4));
        esq.add(embaralharBtn); esq.add(resetBtn);

        JPanel dir = new JPanel(new GridBagLayout());
        dir.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.EAST; gc.gridx = 0; gc.insets = new Insets(2,0,2,0);

        statusLabel = new JLabel("Arraste os blocos do mais leve ao mais pesado!");
        statusLabel.setFont(UiTheme.SMALL_FONT); statusLabel.setForeground(UiTheme.TEXT);
        gc.gridy = 0; dir.add(statusLabel, gc);

        contadorLabel = new JLabel(" ");
        contadorLabel.setFont(UiTheme.SMALL_FONT); contadorLabel.setForeground(UiTheme.TEXT_MUTED);
        gc.gridy = 1; dir.add(contadorLabel, gc);

        panel.add(esq, BorderLayout.CENTER);
        panel.add(dir, BorderLayout.EAST);
        return panel;
    }

    private void alternarModo() {
        pararAuto();
        modo = (modo == Modo.LIVRE) ? Modo.GUIADO : Modo.LIVRE;
        modoBtn.setText("Modo: " + (modo==Modo.LIVRE ? "Livre" : "Guiado"));
        if (modo == Modo.GUIADO) { UiTheme.stylePrimaryButton(modoBtn); modoBtn.setForeground(new Color(15,18,35)); }
        else                     { UiTheme.styleSecondaryButton(modoBtn); }
        pontosLabel.setVisible(modo == Modo.GUIADO);
        resetarEstado();
        explicacaoArea.setText(explicacaoInicial());
        instructionBanner.limpar();
        boardPanel.repaint();
        setStatus(modo==Modo.LIVRE ? "Modo Livre: arraste os blocos ou simule passo a passo."
                                   : "Modo Guiado: siga as instrucoes e execute as acoes!");
    }

    private void gerarPassos() {
        int[] copia = Arrays.copyOf(valores, N);
        passos = new ArrayList<>();
        switch ((String)algoCombo.getSelectedItem()) {
            case "Bubble Sort":    gerarBubble(copia);    break;
            case "Selection Sort": gerarSelection(copia); break;
            case "Insertion Sort": gerarInsertion(copia); break;
            case "Quick Sort":     gerarQuick(copia,0,copia.length-1); break;
            case "Merge Sort":     gerarMerge(copia,0,copia.length-1); break;
        }
        passoAtual = -1; comparacoes = 0; swaps = 0; atualizarUI();
    }

    private void gerarBubble(int[] a) {
        for (int i=0;i<a.length-1;i++) for (int j=0;j<a.length-1-i;j++) {
            passos.add(new int[]{j,j+1,0});
            if (a[j]>a[j+1]) { passos.add(new int[]{j,j+1,1}); int t=a[j];a[j]=a[j+1];a[j+1]=t; }
        }
    }
    private void gerarSelection(int[] a) {
        for (int i=0;i<a.length-1;i++) {
            int min=i;
            for (int j=i+1;j<a.length;j++) { passos.add(new int[]{min,j,0}); if(a[j]<a[min]) min=j; }
            if (min!=i) { passos.add(new int[]{i,min,1}); int t=a[i];a[i]=a[min];a[min]=t; }
        }
    }
    private void gerarInsertion(int[] a) {
        for (int i=1;i<a.length;i++) { int j=i;
            while (j>0) { passos.add(new int[]{j-1,j,0});
                if (a[j-1]>a[j]) { passos.add(new int[]{j-1,j,1}); int t=a[j];a[j]=a[j-1];a[j-1]=t; j--; }
                else break; }
        }
    }
    private void gerarQuick(int[] a, int lo, int hi) {
        if (lo>=hi) return; int pivot=a[hi],i=lo-1;
        for (int j=lo;j<hi;j++) { passos.add(new int[]{j,hi,0});
            if (a[j]<=pivot) { i++; if(i!=j){passos.add(new int[]{i,j,1});int t=a[i];a[i]=a[j];a[j]=t;} } }
        i++; if(i!=hi){passos.add(new int[]{i,hi,1});int t=a[i];a[i]=a[hi];a[hi]=t;}
        gerarQuick(a,lo,i-1); gerarQuick(a,i+1,hi);
    }
    private void gerarMerge(int[] a, int lo, int hi) {
        if (lo>=hi) return; int mid=(lo+hi)/2;
        gerarMerge(a,lo,mid); gerarMerge(a,mid+1,hi);
        int[] tmp=Arrays.copyOfRange(a,lo,hi+1); int i=0,j=mid-lo+1,k=lo;
        while (i<=mid-lo&&j<=hi-lo) { passos.add(new int[]{lo+i,lo+j,0});
            if(tmp[i]<=tmp[j]) a[k++]=tmp[i++];
            else {passos.add(new int[]{k,lo+j,1});a[k++]=tmp[j++];} }
        while(i<=mid-lo) a[k++]=tmp[i++]; while(j<=hi-lo) a[k++]=tmp[j++];
    }

    private void proximoPasso() { pararAuto(); avancarPasso(); }

    private void avancarPasso() {
        if (passos == null) gerarPassos();
        passoAtual++;
        if (passoAtual >= passos.size()) { concluirOrdenacao(); return; }

        int[] passo = passos.get(passoAtual);
        int a = passo[0], b = passo[1];
        isSwap = passo[2] == 1;
        highlightA = a; highlightB = b; mostraSeta = false;

        if (modo == Modo.LIVRE) {
            if (isSwap) {
                swaps++;
                executarSwapAnimado(a, b, () -> boardPanel.repaint());
            } else {
                comparacoes++;
                boolean maior = valores[a] > valores[b];
                compA = maior?a:b; mostraSeta = true;
            }
            setStatus(isSwap
                ? "Troca: ["+a+"] <-> ["+b+"]   Comp: "+comparacoes+"  Trocas: "+swaps
                : "Comparando ["+a+"] e ["+b+"]: "+valores[a]+(valores[a]>valores[b]?" > ":" <= ")+valores[b]);
            explicacaoArea.setText(gerarExplicacaoPasso(a, b, isSwap));
            instructionBanner.limpar();
        } else {
            aguardandoAcao = true;
            acaoEsperadaA = a; acaoEsperadaB = b; acaoEhSwap = isSwap;
            comparacoes++;
            boolean maior = valores[a] > valores[b];
            compA = maior?a:b; mostraSeta = true;

            boolean precisaTrocar = isSwap;
            okBtn.setVisible(!precisaTrocar);
            passoBtn.setEnabled(false);
            autoBtn.setEnabled(false);

            if (precisaTrocar) {
                instructionBanner.mostrar(
                    "TROQUE os blocos " + a + " e " + b + "!",
                    valores[a] + " > " + valores[b] + " — arraste um sobre o outro",
                    InstructionBanner.Tipo.TROCAR
                );
            } else {
                instructionBanner.mostrar(
                    "Ja estao na ordem certa!",
                    valores[a] + " <= " + valores[b] + " — clique em  OK — sem troca",
                    InstructionBanner.Tipo.OK
                );
            }
            iniciarDicaPiscante();
            explicacaoArea.setText(gerarExplicacaoPasso(a, b, isSwap));
        }

        progressBar.setProgresso(passoAtual, passos.size());
        atualizarUI();
        boardPanel.repaint();
    }

    private void confirmarSemTroca() {
        if (!aguardandoAcao || acaoEhSwap) return;
        aguardandoAcao = false;
        okBtn.setVisible(false);
        pararDicaPiscante();
        acertosSeq++; pontos += 10 + acertosSeq*2;
        mostrarBanner("CORRETO!", UiTheme.SUCCESS);
        animarAcerto();
        passoBtn.setEnabled(true); autoBtn.setEnabled(true);
        instructionBanner.mostrarAcerto("Certo! Nenhuma troca necessaria. Clique em  Proximo passo.");
        setStatus("Correto! Os blocos ja estavam na ordem certa.");
        atualizarUI();
    }

    private void onSwapGuiado(int idxA, int idxB) {
        if (!aguardandoAcao) return;
        boolean correto = acaoEhSwap &&
            ((idxA==acaoEsperadaA&&idxB==acaoEsperadaB)||(idxA==acaoEsperadaB&&idxB==acaoEsperadaA));

        if (correto) {
            aguardandoAcao = false;
            okBtn.setVisible(false);
            pararDicaPiscante();
            swaps++; acertosSeq++; pontos += 20 + acertosSeq*3;
            executarSwapAnimado(idxA, idxB, () -> {
                mostrarBanner("CORRETO!", UiTheme.SUCCESS);
                animarAcerto();
                passoBtn.setEnabled(true); autoBtn.setEnabled(true);
                instructionBanner.mostrarAcerto("Troca feita! Clique em  Proximo passo  para continuar.");
                setStatus("Correto! Comp: "+comparacoes+"  Trocas: "+swaps);
                atualizarUI(); boardPanel.repaint();
            });
        } else {
            erros++; acertosSeq = 0; pontos = Math.max(0, pontos-5);
            mostrarBanner("ERRADO!", UiTheme.DANGER);
            animarErro();
            String dica = acaoEhSwap
                ? "Arraste a posicao "+acaoEsperadaA+" sobre a posicao "+acaoEsperadaB+"."
                : "Estes blocos ja estao na ordem! Clique em  OK — sem troca.";
            instructionBanner.mostrarErro("Tente de novo! " + dica);
            setStatus("Errado! " + dica);
            atualizarUI();
        }
    }

    private void onCliqueGuiado(int idx) {
        if (!aguardandoAcao || acaoEhSwap) return;
        if (idx == acaoEsperadaA || idx == acaoEsperadaB) confirmarSemTroca();
    }

    private void concluirOrdenacao() {
        highlightA = -1; highlightB = -1; isSwap = false;
        mostraSeta = false; aguardandoAcao = false;
        okBtn.setVisible(false);
        passoBtn.setEnabled(false); autoBtn.setEnabled(false);
        progressBar.setProgresso(passos.size(), passos.size());
        pararDicaPiscante();
        String msg = modo==Modo.GUIADO
            ? "Ordenacao concluida! Pontos: "+pontos+"  Erros: "+erros
            : "Ordenacao concluida! Comp: "+comparacoes+"  Trocas: "+swaps;
        setStatus(msg);
        explicacaoArea.setText(gerarExplicacaoConclusao());
        instructionBanner.mostrar("ORDENADO!", "Parabens! O array esta em ordem crescente.", InstructionBanner.Tipo.CONCLUIDO);
        boardPanel.lancarConfetes();
        boardPanel.repaint();
    }

    private void executarSwapAnimado(int a, int b, Runnable onDone) {
        int tv = valores[a]; valores[a] = valores[b]; valores[b] = tv;
        Color tc = coresBlocos[a]; coresBlocos[a] = coresBlocos[b]; coresBlocos[b] = tc;
        animandoSwap = true; swapIdxA = a; swapIdxB = b; swapProgress = 0f;
        if (swapTimer != null) swapTimer.stop();
        swapTimer = new Timer(14, e -> {
            swapProgress += 0.055f;
            if (swapProgress >= 1f) {
                swapProgress = 1f; animandoSwap = false; ((Timer)e.getSource()).stop();
                if (onDone != null) onDone.run();
            }
            boardPanel.repaint();
        });
        swapTimer.start();
    }

    private void animarAcerto() {
        pulsing = true; pulsePhase = 0f;
        if (pulseTimer != null) pulseTimer.stop();
        pulseTimer = new Timer(18, e -> {
            pulsePhase += 0.13f;
            if (pulsePhase >= (float)(Math.PI*2.5)) { pulsing = false; ((Timer)e.getSource()).stop(); }
            boardPanel.repaint();
        });
        pulseTimer.start();
    }

    private void animarErro() {
        shakingA = true; shakingB = true; shakePhase = 0f;
        if (shakeTimer != null) shakeTimer.stop();
        shakeTimer = new Timer(16, e -> {
            shakePhase += 0.45f;
            if (shakePhase >= (float)(Math.PI*3.5)) {
                shakingA = false; shakingB = false; ((Timer)e.getSource()).stop();
            }
            boardPanel.repaint();
        });
        shakeTimer.start();
    }

    private void iniciarDicaPiscante() {
        pararDicaPiscante();
        dicaPhase = 0f;
        dicaTimer = new Timer(22, e -> { dicaPhase += 0.09f; boardPanel.repaint(); });
        dicaTimer.start();
    }

    private void pararDicaPiscante() {
        if (dicaTimer != null) { dicaTimer.stop(); dicaTimer = null; }
        dicaPhase = 0f;
    }

    private void mostrarBanner(String txt, Color cor) {
        bannerTxt = txt; bannerCor = cor;
        bannerAlpha = 1f;
        bannerY = boardPanel.blocoY() - 20f;
        if (bannerTimer != null) bannerTimer.stop();
        bannerTimer = new Timer(20, e -> {
            bannerAlpha -= 0.025f;
            bannerY     -= 1.2f;
            if (bannerAlpha <= 0) { bannerAlpha = 0; bannerTxt = null; ((Timer)e.getSource()).stop(); }
            boardPanel.repaint();
        });
        bannerTimer.start();
    }

    private void toggleAuto() {
        if (modo == Modo.GUIADO) return;
        if (autoRodando) {
            pararAuto();
        } else {
            autoRodando = true; autoBtn.setText("Pausar"); passoBtn.setEnabled(false);
            if (passos == null) gerarPassos();
            autoTimer = new Timer(600, e -> {
                if (!autoRodando) return;
                avancarPasso();
                if (passoAtual >= (passos==null?0:passos.size())) pararAuto();
            });
            autoTimer.start();
        }
    }

    private void pararAuto() {
        autoRodando = false;
        if (autoTimer != null) autoTimer.stop();
        autoBtn.setText("Executar auto"); passoBtn.setEnabled(true);
    }

    private void setStatus(String msg) { if (statusLabel != null) statusLabel.setText(msg); }

    private void atualizarUI() {
        if (contadorLabel == null) return;
        contadorLabel.setText("Comparacoes: "+comparacoes+"  Trocas: "+swaps);
        if (modo == Modo.GUIADO) {
            pontosLabel.setText("Pontos: "+pontos+"  |  Erros: "+erros+(acertosSeq>=3?"  COMBO x"+acertosSeq+"!":""));
            pontosLabel.setForeground(acertosSeq>=3 ? UiTheme.GOLD : UiTheme.BLUE_LIGHT);
        }
    }

    private boolean estaOrdenado() {
        for (int i=0;i<N-1;i++) if (valores[i]>valores[i+1]) return false; return true;
    }

    private String explicacaoInicial() {
        String algo = algoCombo!=null?(String)algoCombo.getSelectedItem():"Bubble Sort";
        switch (algo) {
            case "Bubble Sort":    return "Bubble Sort\n\nCompara pares adjacentes e troca se estiverem fora de ordem.\n\nO maior elemento 'borbulha' para o final a cada passagem.\n\nComplexidade: O(n²).";
            case "Selection Sort": return "Selection Sort\n\nEncontra o menor elemento e o coloca na posicao correta.\n\nA parte esquerda cresce ordenada a cada rodada.\n\nComplexidade: O(n²).";
            case "Insertion Sort": return "Insertion Sort\n\nInsere cada elemento na posicao correta da parte ja ordenada.\n\nEficiente para arrays quase ordenados.\n\nComplexidade: O(n²) pior, O(n) melhor.";
            case "Quick Sort":     return "Quick Sort\n\nEscolhe um pivo e particiona: menores a esq, maiores a dir.\n\nAplica recursivamente em cada particao.\n\nComplexidade: O(n log n) medio.";
            case "Merge Sort":     return "Merge Sort\n\nDivide ao meio recursivamente e depois mescla em ordem.\n\nGarante O(n log n) em todos os casos.";
            default: return "";
        }
    }

    private String gerarExplicacaoPasso(int a, int b, boolean swap) {
        String algo = (String) algoCombo.getSelectedItem();
        String base = algo + "\n\n";
        if (swap) return base+"TROCA!\n\nPosicao "+a+" ("+valores[b]+") e posicao "+b+" ("+valores[a]+") estavam fora de ordem.\n\nTrocados para que o menor fique a esquerda.";
        boolean fora = valores[a] > valores[b];
        String res = fora ? "Fora de ordem — troca necessaria." : "Na ordem certa — sem troca.";
        switch (algo) {
            case "Bubble Sort":    return base+"Comparando posicoes adjacentes "+a+" e "+b+".\n\n["+a+"] = "+valores[a]+"   ["+b+"] = "+valores[b]+"\n\n"+res;
            case "Selection Sort": return base+"Buscando minimo no trecho nao ordenado.\n\nMinimo atual: "+a+" ("+valores[a]+")\nCandidato: "+b+" ("+valores[b]+")\n\n"+(valores[b]<valores[a]?"Novo minimo encontrado!":"Minimo continua o mesmo.");
            case "Insertion Sort": return base+"Inserindo elemento na posicao correta.\n\n["+a+"] = "+valores[a]+"   ["+b+"] = "+valores[b]+"\n\n"+res;
            case "Quick Sort":     return base+"Pivo: posicao "+b+" (valor "+valores[b]+")\n\nElemento: posicao "+a+" (valor "+valores[a]+")\n\n"+(valores[a]<=valores[b]?"<= pivo: vai para esquerda.":"> pivo: fica a direita.");
            case "Merge Sort":     return base+"Mesclando duas metades.\n\n["+a+"] = "+valores[a]+"   ["+b+"] = "+valores[b]+"\n\nO menor entra primeiro na sequencia final.";
            default: return base;
        }
    }

    private String gerarExplicacaoConclusao() {
        String stats = modo==Modo.GUIADO ? "\n\nPontuacao: "+pontos+"  Erros: "+erros : "\n\nComp: "+comparacoes+"  Trocas: "+swaps;
        return (String)algoCombo.getSelectedItem()+" concluido!\n\nArray ordenado do menor para o maior."+stats+"\n\nExperimente outro algoritmo!";
    }

    private static class InstructionBanner extends JPanel {

        enum Tipo { TROCAR, OK, ACERTO, ERRO, CONCLUIDO, VAZIO }

        private String linha1 = "", linha2 = "";
        private Tipo   tipo   = Tipo.VAZIO;
        private float  pulso  = 0f;
        private Timer  pulsoTimer;

        InstructionBanner() {
            setOpaque(false);
            pulsoTimer = new Timer(30, e -> { pulso += 0.12f; repaint(); });
        }

        void mostrar(String l1, String l2, Tipo t) {
            linha1 = l1; linha2 = l2; tipo = t; pulso = 0f;
            if (t == Tipo.TROCAR || t == Tipo.OK) pulsoTimer.start();
            else pulsoTimer.stop();
            repaint();
        }

        void mostrarAcerto(String l2) { mostrar("CORRETO!", l2, Tipo.ACERTO); }
        void mostrarErro  (String l2) { mostrar("ERRADO!",  l2, Tipo.ERRO);   }

        void limpar() {
            linha1 = ""; linha2 = ""; tipo = Tipo.VAZIO;
            pulsoTimer.stop(); repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (tipo == Tipo.VAZIO) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            Color bgBase, fgCor;
            switch (tipo) {
                case TROCAR:   bgBase = new Color(200,60,60);   fgCor = Color.WHITE; break;
                case OK:       bgBase = new Color(200,155,30);  fgCor = Color.WHITE; break;
                case ACERTO:   bgBase = new Color(40,170,100);  fgCor = Color.WHITE; break;
                case ERRO:     bgBase = new Color(190,50,50);   fgCor = Color.WHITE; break;
                case CONCLUIDO:bgBase = new Color(70,130,220);  fgCor = Color.WHITE; break;
                default:       bgBase = new Color(50,60,90);    fgCor = Color.WHITE; break;
            }

            float brilho = 0f;
            if (tipo == Tipo.TROCAR || tipo == Tipo.OK)
                brilho = (float)(Math.sin(pulso) * 0.18 + 0.18);

            Color bgFim = bgBase.brighter();
            g2.setPaint(new GradientPaint(0, 0,
                new Color(bgBase.getRed(), bgBase.getGreen(), bgBase.getBlue(),
                          (int)(200 + brilho*55)),
                w, 0,
                new Color(bgFim.getRed(), bgFim.getGreen(), bgFim.getBlue(),
                          (int)(200 + brilho*55))));
            g2.fill(new RoundRectangle2D.Float(4, 4, w-8, h-8, 16, 16));

            g2.setColor(new Color(255,255,255,(int)(60+brilho*120)));
            g2.setStroke(new BasicStroke(1.8f));
            g2.draw(new RoundRectangle2D.Float(5,5,w-10,h-10,16,16));

            g2.setFont(new Font("Poppins", Font.BOLD, 17));
            g2.setColor(new Color(0,0,0,60));
            g2.drawString(linha1, 19, 27);
            g2.setColor(fgCor);
            g2.drawString(linha1, 18, 26);

            g2.setFont(new Font("Poppins", Font.PLAIN, 12));
            g2.setColor(new Color(fgCor.getRed(), fgCor.getGreen(), fgCor.getBlue(), 210));
            g2.drawString(linha2, 18, 48);

            g2.dispose();
        }
    }

    private class BoardPanel extends JPanel {

        private java.util.List<float[]> confetes = new ArrayList<>();
        private Timer confeteTimer;

        BoardPanel() {
            setBackground(UiTheme.BG); setOpaque(true);
            MouseAdapter ma = new MouseAdapter() {
                @Override public void mousePressed (MouseEvent e) { onPress(e);   }
                @Override public void mouseDragged (MouseEvent e) { onDrag(e);    }
                @Override public void mouseReleased(MouseEvent e) { onRelease(e); }
            };
            addMouseListener(ma); addMouseMotionListener(ma);
        }

        private void onPress(MouseEvent e) {
            if (animandoSwap) return;
            int idx = blocoNaPosicao(e.getX(), e.getY());
            if (idx < 0) return;
            if (modo==Modo.GUIADO && !aguardandoAcao) return;
            if (modo==Modo.GUIADO && !acaoEhSwap) { onCliqueGuiado(idx); return; }
            if (modo==Modo.LIVRE && passoAtual >= 0) return;
            dragging = idx; dragOffX = e.getX()-blocoX(idx); dragX = e.getX();
            repaint();
        }

        private void onDrag(MouseEvent e) {
            if (dragging<0||animandoSwap) return;
            dragX = e.getX();
            dropTarget = slotMaisProximo(e.getX()); repaint();
        }

        private void onRelease(MouseEvent e) {
            if (dragging<0) return;
            int alvo = slotMaisProximo(e.getX()), from = dragging;
            dragging = -1; dropTarget = -1;
            if (alvo>=0 && alvo!=from) {
                if (modo==Modo.GUIADO) { onSwapGuiado(from, alvo); }
                else { deslocarBlocos(from, alvo); if (estaOrdenado()) { setStatus("Parabens!"); lancarConfetes(); } }
            }
            repaint();
        }

        private void deslocarBlocos(int from, int alvo) {
            int tmp=valores[from]; Color tc=coresBlocos[from];
            if (alvo>from) { for(int i=from;i<alvo;i++){valores[i]=valores[i+1];coresBlocos[i]=coresBlocos[i+1];} }
            else           { for(int i=from;i>alvo;i--){valores[i]=valores[i-1];coresBlocos[i]=coresBlocos[i-1];} }
            valores[alvo]=tmp; coresBlocos[alvo]=tc;
        }

        private int blocoNaPosicao(int mx, int my) {
            for (int i=0;i<N;i++) {
                if (i==dragging) continue;
                int bx=blocoX(i), by=blocoY();
                if (mx>=bx&&mx<=bx+BLOCK_W&&my>=by&&my<=by+BLOCK_H) return i;
            } return -1;
        }

        private int slotMaisProximo(int mx) {
            int best=-1; double min=Double.MAX_VALUE;
            for (int i=0;i<N;i++) { double d=Math.abs(mx-(blocoX(i)+BLOCK_W/2.0)); if(d<min){min=d;best=i;} }
            return best;
        }

        int blocoX(int i) { int total=N*BLOCK_W+(N-1)*BLOCK_GAP; return (getWidth()-total)/2+i*(BLOCK_W+BLOCK_GAP); }
        int blocoY()      { return BOARD_Y+(getHeight()-BOARD_Y-BLOCK_H-60)/2; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setColor(UiTheme.BG); g2.fillRect(0,0,getWidth(),getHeight());

            desenharEscala(g2);
            desenharSlots(g2);

            if (mostraSeta && compA>=0 && !isSwap) desenharSeta(g2);

            for (int i=0;i<N;i++) {
                if (i==dragging) continue;
                int ox=0, oy=0;
                if ((shakingA&&i==highlightA)||(shakingB&&i==highlightB))
                    ox = (int)(Math.sin(shakePhase*3)*7);
                if (animandoSwap && (i==swapIdxA||i==swapIdxB)) {
                    float t = swapProgress;
                    float arc = (float)(-Math.sin(t*Math.PI)*34);
                    oy = (int) arc;
                    int xA = blocoX(swapIdxA), xB = blocoX(swapIdxB);
                    if (i == swapIdxA) ox = (int)((xB-xA)*t) - (blocoX(i)-xA);
                    else               ox = (int)((xA-xB)*t) - (blocoX(i)-xB);
                }
                desenharBloco(g2, i, blocoX(i)+ox, blocoY()+oy);
            }

            if (pulsing && highlightA>=0) { desenharPulso(g2, highlightA); if (highlightB>=0) desenharPulso(g2, highlightB); }

            if (aguardandoAcao && acaoEsperadaA>=0) {
                float alpha = (float)(Math.sin(dicaPhase)*0.4+0.6);
                desenharDica(g2, acaoEsperadaA, alpha);
                if (acaoEhSwap && acaoEsperadaB>=0) desenharDica(g2, acaoEsperadaB, alpha);
            }

            if (dragging>=0) desenharBlocoArrastado(g2, dragging, dragX-dragOffX, blocoY()-12);

            if (dragging>=0 && dropTarget>=0 && dropTarget!=dragging) {
                int sx=blocoX(dropTarget), sy=blocoY();
                g2.setColor(new Color(90,155,255,55)); g2.fillRoundRect(sx,sy,BLOCK_W,BLOCK_H,14,14);
                g2.setColor(new Color(90,155,255,160));
                g2.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{5,4},0));
                g2.drawRoundRect(sx,sy,BLOCK_W,BLOCK_H,14,14);
            }

            if (bannerTxt != null && bannerAlpha > 0) desenharBannerFlutuante(g2);

            desenharConfetes(g2);
            g2.dispose();
        }

        private void desenharEscala(Graphics2D g2) {
            int total=N*BLOCK_W+(N-1)*BLOCK_GAP, startX=(getWidth()-total)/2;
            int by=blocoY()+BLOCK_H+18;
            g2.setFont(new Font("Poppins",Font.BOLD,11)); g2.setColor(UiTheme.TEXT_MUTED);
            g2.drawString("Mais leve", startX, by);
            int ax1=startX+68, ax2=startX+total-70;
            g2.setColor(new Color(80,95,140));
            g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{4,3},0));
            g2.drawLine(ax1,by-4,ax2,by-4);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(ax2-8,by-8,ax2,by-4); g2.drawLine(ax2-8,by,ax2,by-4);
            String txt="Mais pesado";
            g2.setColor(UiTheme.TEXT_MUTED);
            g2.drawString(txt,startX+total-g2.getFontMetrics().stringWidth(txt),by);
        }

        private void desenharSlots(Graphics2D g2) {
            int by=blocoY();
            for (int i=0;i<N;i++) {
                int bx=blocoX(i);
                g2.setColor(new Color(30,36,58)); g2.fillRoundRect(bx,by,BLOCK_W,BLOCK_H,14,14);
                g2.setColor(new Color(50,62,95));
                g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{5,4},0));
                g2.drawRoundRect(bx,by,BLOCK_W,BLOCK_H,14,14);
            }
        }

        private void desenharBloco(Graphics2D g2, int idx, int bx, int by) {
            Color cor=coresBlocos[idx]; int peso=valores[idx];
            boolean hi=idx==highlightA||idx==highlightB;
            g2.setColor(new Color(0,0,0,hi?90:40)); g2.fillRoundRect(bx+3,by+5,BLOCK_W,BLOCK_H,14,14);
            g2.setPaint(new GradientPaint(bx,by,hi?cor.brighter():cor,bx,by+BLOCK_H,cor.darker()));
            g2.fillRoundRect(bx,by,BLOCK_W,BLOCK_H,14,14);
            g2.setColor(new Color(255,255,255,hi?70:45)); g2.fillRoundRect(bx+3,by+3,BLOCK_W-6,BLOCK_H/3,10,10);
            if (hi) {
                Color borda = isSwap ? UiTheme.GOLD : new Color(255,255,255,200);
                g2.setColor(borda); g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(bx+1,by+1,BLOCK_W-2,BLOCK_H-2,14,14);
            }
            g2.setFont(new Font("Poppins",Font.BOLD,22)); FontMetrics fm=g2.getFontMetrics();
            String txt=String.valueOf(peso);
            int tx=bx+(BLOCK_W-fm.stringWidth(txt))/2, ty=by+(BLOCK_H+fm.getAscent()-fm.getDescent())/2;
            g2.setColor(new Color(0,0,0,80)); g2.drawString(txt,tx+1,ty+1);
            g2.setColor(Color.WHITE);         g2.drawString(txt,tx,ty);
        }

        private void desenharBlocoArrastado(Graphics2D g2, int idx, int bx, int by) {
            Color cor=coresBlocos[idx];
            g2.setColor(new Color(0,0,0,110)); g2.fillRoundRect(bx+7,by+11,BLOCK_W,BLOCK_H,14,14);
            g2.setPaint(new GradientPaint(bx,by,cor.brighter(),bx,by+BLOCK_H,cor));
            g2.fillRoundRect(bx-2,by-2,BLOCK_W+4,BLOCK_H+4,14,14);
            g2.setColor(new Color(255,255,255,80)); g2.fillRoundRect(bx+1,by+1,BLOCK_W,BLOCK_H/3,10,10);
            g2.setFont(new Font("Poppins",Font.BOLD,22)); FontMetrics fm=g2.getFontMetrics();
            String txt=String.valueOf(valores[idx]);
            int tx=bx+(BLOCK_W-fm.stringWidth(txt))/2, ty=by+(BLOCK_H+fm.getAscent()-fm.getDescent())/2;
            g2.setColor(new Color(0,0,0,80)); g2.drawString(txt,tx+1,ty+1);
            g2.setColor(Color.WHITE);         g2.drawString(txt,tx,ty);
        }

        private void desenharSeta(Graphics2D g2) {
            int pesadoIdx=valores[highlightA]>valores[highlightB]?highlightA:highlightB;
            int leveIdx=pesadoIdx==highlightA?highlightB:highlightA;
            int lx=blocoX(leveIdx)+BLOCK_W/2, px=blocoX(pesadoIdx)+BLOCK_W/2, y=blocoY()-30;
            g2.setColor(new Color(220,60,60));
            g2.setStroke(new BasicStroke(2.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            g2.drawLine(lx,y,px,y);
            int d=px>lx?1:-1;
            g2.fillPolygon(new int[]{px,px-d*12,px-d*12},new int[]{y,y-7,y+7},3);
            g2.setFont(new Font("Poppins",Font.BOLD,11)); g2.setColor(new Color(220,60,60));
            String lbl="mais pesado"; int lxL=px+d*6;
            if (d<0) lxL-=g2.getFontMetrics().stringWidth(lbl);
            g2.drawString(lbl,lxL,y-10);
        }

        private void desenharPulso(Graphics2D g2, int idx) {
            int bx=blocoX(idx), by=blocoY();
            float alpha=(float)(Math.sin(pulsePhase)*0.5+0.5);
            float expand=(float)(Math.sin(pulsePhase)*10);
            g2.setColor(new Color(70,210,130,(int)(alpha*200)));
            g2.setStroke(new BasicStroke(3.5f));
            g2.draw(new RoundRectangle2D.Float(bx-expand/2,by-expand/2,BLOCK_W+expand,BLOCK_H+expand,18,18));
        }

        private void desenharDica(Graphics2D g2, int idx, float alpha) {
            int bx=blocoX(idx), by=blocoY();
            Color fill = acaoEhSwap ? new Color(220,60,60,(int)(alpha*70)) : new Color(200,155,30,(int)(alpha*70));
            g2.setColor(fill); g2.fillRoundRect(bx,by,BLOCK_W,BLOCK_H,14,14);
            Color borda = acaoEhSwap ? new Color(255,80,80,(int)(alpha*255)) : new Color(255,205,70,(int)(alpha*255));
            g2.setColor(borda);
            g2.setStroke(new BasicStroke(3.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{7,4},0));
            g2.drawRoundRect(bx+2,by+2,BLOCK_W-4,BLOCK_H-4,12,12);
        }

        private void desenharBannerFlutuante(Graphics2D g2) {
            g2.setFont(new Font("Poppins",Font.BOLD,36));
            FontMetrics fm=g2.getFontMetrics();
            int tw=fm.stringWidth(bannerTxt);
            int tx=(getWidth()-tw)/2, ty=(int)bannerY;
            g2.setColor(new Color(0,0,0,(int)(bannerAlpha*100)));
            g2.drawString(bannerTxt,tx+2,ty+2);
            Color bc=bannerCor;
            g2.setColor(new Color(bc.getRed(),bc.getGreen(),bc.getBlue(),(int)(bannerAlpha*255)));
            g2.drawString(bannerTxt,tx,ty);
        }

        void lancarConfetes() {
            confetes.clear(); Random rng=new Random();
            for (int i=0;i<90;i++)
                confetes.add(new float[]{getWidth()/2f+(rng.nextFloat()-0.5f)*getWidth(),-10,
                    (rng.nextFloat()-0.5f)*6f, 2f+rng.nextFloat()*5f, 5+rng.nextFloat()*10f, rng.nextInt(PALETA.length)});
            if (confeteTimer!=null) confeteTimer.stop();
            confeteTimer=new Timer(28,e->{
                confetes.removeIf(c->{c[0]+=c[2];c[1]+=c[3];c[2]*=0.99f;return c[1]>getHeight()+20;});
                repaint(); if(confetes.isEmpty()) ((Timer)e.getSource()).stop();
            });
            confeteTimer.start();
        }

        private void desenharConfetes(Graphics2D g2) {
            for (float[] c:confetes) {
                g2.setColor(PALETA[(int)c[5]]);
                g2.fillRoundRect((int)c[0],(int)c[1],(int)c[4],(int)(c[4]*0.6f),3,3);
            }
        }
    }

    private static class ProgressBarPanel extends JPanel {
        private int atual=0, total=1;
        ProgressBarPanel() { setOpaque(false); }
        void setProgresso(int a, int t) { atual=a; total=Math.max(1,t); repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(),h=getHeight(),barH=10,barY=(h-barH)/2;
            g2.setColor(UiTheme.PANEL_DARK);
            g2.fill(new RoundRectangle2D.Float(0,barY,w,barH,barH,barH));
            int fw=(int)((float)atual/total*w);
            if (fw>0) {
                g2.setPaint(new GradientPaint(0,0,UiTheme.BLUE,fw,0,UiTheme.BLUE_LIGHT));
                g2.fill(new RoundRectangle2D.Float(0,barY,fw,barH,barH,barH));
            }
            g2.setFont(new Font("Poppins",Font.BOLD,10)); g2.setColor(UiTheme.TEXT_MUTED);
            String txt="Passo "+atual+" de "+total;
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString(txt,(w-fm.stringWidth(txt))/2,barY+barH-1);
            g2.dispose();
        }
    }
}