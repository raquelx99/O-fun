package game;

import model.TipoEntrada;

public class ChallengeScenario {
    private String titulo;
    private String descricao;
    private int tamanhoEntrada;
    private TipoEntrada tipoEntrada;

    public ChallengeScenario(
            String titulo,
            String descricao,
            int tamanhoEntrada,
            TipoEntrada tipoEntrada
    ) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.tamanhoEntrada = tamanhoEntrada;
        this.tipoEntrada = tipoEntrada;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getTamanhoEntrada() {
        return tamanhoEntrada;
    }

    public TipoEntrada getTipoEntrada() {
        return tipoEntrada;
    }
}