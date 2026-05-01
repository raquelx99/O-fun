package game;

public class ScoreCalculator {

    public static double calculate(int tamanhoEntrada, double tempoMs) {
        if (tempoMs <= 0) {
            return 0;
        }

        return tamanhoEntrada / tempoMs;
    }
}