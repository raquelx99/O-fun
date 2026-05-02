package benchmark;

import java.util.List;

import model.ResultadoBenchmark;

public class BenchmarkStats {

    public static double calcularMedia(List<ResultadoBenchmark> resultados) {
        double soma = 0;

        for (ResultadoBenchmark resultado : resultados) {
            soma += resultado.getTempoMs();
        }

        return soma / resultados.size();
    }

    public static double calcularMenorTempo(List<ResultadoBenchmark> resultados) {
        double menor = Double.MAX_VALUE;

        for (ResultadoBenchmark resultado : resultados) {
            if (resultado.getTempoMs() < menor) {
                menor = resultado.getTempoMs();
            }
        }

        return menor;
    }

    public static double calcularMaiorTempo(List<ResultadoBenchmark> resultados) {
        double maior = Double.MIN_VALUE;

        for (ResultadoBenchmark resultado : resultados) {
            if (resultado.getTempoMs() > maior) {
                maior = resultado.getTempoMs();
            }
        }

        return maior;
    }

    public static boolean todosOrdenadosCorretamente(List<ResultadoBenchmark> resultados) {
        for (ResultadoBenchmark resultado : resultados) {
            if (!resultado.isOrdenadoCorretamente()) {
                return false;
            }
        }

        return true;
    }
}