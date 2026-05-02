package model;

public class ResultadoBenchmark {
    private String algoritmo;
    private String versao;
    private int threads;
    private int tamanhoEntrada;
    private String tipoEntrada;
    private int amostra;
    private double tempoMs;
    private double score;
    private boolean ordenadoCorretamente;

    public ResultadoBenchmark(
            String algoritmo,
            String versao,
            int threads,
            int tamanhoEntrada,
            String tipoEntrada,
            int amostra,
            double tempoMs,
            double score,
            boolean ordenadoCorretamente
    ) {
        this.algoritmo = algoritmo;
        this.versao = versao;
        this.threads = threads;
        this.tamanhoEntrada = tamanhoEntrada;
        this.tipoEntrada = tipoEntrada;
        this.amostra = amostra;
        this.tempoMs = tempoMs;
        this.score = score;
        this.ordenadoCorretamente = ordenadoCorretamente;
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

    public int getAmostra() {
        return amostra;
    }

    public double getTempoMs() {
        return tempoMs;
    }

    public double getScore() {
        return score;
    }

    public boolean isOrdenadoCorretamente() {
        return ordenadoCorretamente;
    }

    public String toCsvLine() {
        return algoritmo + "," +
                versao + "," +
                threads + "," +
                tamanhoEntrada + "," +
                tipoEntrada + "," +
                amostra + "," +
                tempoMs + "," +
                score + "," +
                ordenadoCorretamente;
    }
}