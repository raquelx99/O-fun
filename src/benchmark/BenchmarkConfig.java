package benchmark;

import model.TipoEntrada;

public class BenchmarkConfig {
    private int tamanhoEntrada;
    private TipoEntrada tipoEntrada;
    private int amostras;

    public BenchmarkConfig(int tamanhoEntrada, TipoEntrada tipoEntrada, int amostras) {
        this.tamanhoEntrada = tamanhoEntrada;
        this.tipoEntrada = tipoEntrada;
        this.amostras = amostras;
    }

    public int getTamanhoEntrada() {
        return tamanhoEntrada;
    }

    public TipoEntrada getTipoEntrada() {
        return tipoEntrada;
    }

    public int getAmostras() {
        return amostras;
    }
}