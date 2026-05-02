package game;

import java.util.Map;

public class ChallengeResult {
    private String algoritmoEscolhido;
    private String melhorAlgoritmo;
    private double tempoEscolhido;
    private double melhorTempo;
    private int pontuacao;
    private Map<String, Double> temposPorAlgoritmo;

    public ChallengeResult(
            String algoritmoEscolhido,
            String melhorAlgoritmo,
            double tempoEscolhido,
            double melhorTempo,
            int pontuacao,
            Map<String, Double> temposPorAlgoritmo
    ) {
        this.algoritmoEscolhido = algoritmoEscolhido;
        this.melhorAlgoritmo = melhorAlgoritmo;
        this.tempoEscolhido = tempoEscolhido;
        this.melhorTempo = melhorTempo;
        this.pontuacao = pontuacao;
        this.temposPorAlgoritmo = temposPorAlgoritmo;
    }

    public String getAlgoritmoEscolhido() {
        return algoritmoEscolhido;
    }

    public String getMelhorAlgoritmo() {
        return melhorAlgoritmo;
    }

    public double getTempoEscolhido() {
        return tempoEscolhido;
    }

    public double getMelhorTempo() {
        return melhorTempo;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public Map<String, Double> getTemposPorAlgoritmo() {
        return temposPorAlgoritmo;
    }
}