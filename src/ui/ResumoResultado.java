package ui;

public class ResumoResultado {
    private String algoritmo;
    private String versao;
    private int threads;
    private int tamanhoEntrada;
    private String tipoEntrada;
    private double mediaMs;
    private double menorMs;
    private double maiorMs;
    private double speedup;
    private double eficiencia;
    private boolean todosOrdenadosCorretamente;

    public ResumoResultado(
            String algoritmo,
            String versao,
            int threads,
            int tamanhoEntrada,
            String tipoEntrada,
            double mediaMs,
            double menorMs,
            double maiorMs,
            double speedup,
            double eficiencia,
            boolean todosOrdenadosCorretamente
    ) {
        this.algoritmo = algoritmo;
        this.versao = versao;
        this.threads = threads;
        this.tamanhoEntrada = tamanhoEntrada;
        this.tipoEntrada = tipoEntrada;
        this.mediaMs = mediaMs;
        this.menorMs = menorMs;
        this.maiorMs = maiorMs;
        this.speedup = speedup;
        this.eficiencia = eficiencia;
        this.todosOrdenadosCorretamente = todosOrdenadosCorretamente;
    }

    public String getAlgoritmo() {
        return algoritmo;
    }

    public String getVersao() {
        return versao;
    }

    public int getThreads() {
        return threads;
    }

    public int getTamanhoEntrada() {
        return tamanhoEntrada;
    }

    public String getTipoEntrada() {
        return tipoEntrada;
    }

    public double getMediaMs() {
        return mediaMs;
    }

    public double getMenorMs() {
        return menorMs;
    }

    public double getMaiorMs() {
        return maiorMs;
    }

    public double getSpeedup() {
        return speedup;
    }

    public double getEficiencia() {
        return eficiencia;
    }

    public boolean isTodosOrdenadosCorretamente() {
        return todosOrdenadosCorretamente;
    }
}