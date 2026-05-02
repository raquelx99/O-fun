package game;

public class ScoreCalculator {

    private static final int SCORE_MAXIMO = 10_000;

    /**
     * Calcula a pontuação de uma execução com base no tamanho da entrada e no
     * tempo gasto. Usa escala logarítmica para evitar variação extrema entre
     * algoritmos rápidos e lentos, mantendo os valores comparáveis na UI.
     *
     * @param tamanhoEntrada número de elementos ordenados
     * @param tempoMs        tempo de execução em milissegundos
     * @return pontuação entre 0 e SCORE_MAXIMO
     */
    public static double calculate(int tamanhoEntrada, double tempoMs) {
        if (tempoMs <= 0) {
            return 0;
        }

        double throughput = tamanhoEntrada / tempoMs;

        double scoreRaw = Math.log1p(throughput) * 1000.0;

        return Math.min(SCORE_MAXIMO, scoreRaw);
    }
}