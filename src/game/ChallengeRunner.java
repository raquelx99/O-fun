package game;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import benchmark.BenchmarkConfig;
import benchmark.BenchmarkRunner;
import benchmark.BenchmarkStats;
import model.ResultadoBenchmark;
import sort.BubbleSort;
import sort.InsertionSort;
import sort.MergeSort;
import sort.QuickSort;
import sort.SortAlgorithm;

public class ChallengeRunner {

    private BenchmarkRunner benchmarkRunner;

    public ChallengeRunner() {
        benchmarkRunner = new BenchmarkRunner();
    }

    public ChallengeResult executarDesafio(
            ChallengeScenario scenario,
            String algoritmoEscolhido
    ) {
        BenchmarkConfig config = new BenchmarkConfig(
                scenario.getTamanhoEntrada(),
                scenario.getTipoEntrada(),
                5
        );

        SortAlgorithm[] algoritmos = {
                new BubbleSort(),
                new InsertionSort(),
                new QuickSort(),
                new MergeSort()
        };

        Map<String, Double> temposPorAlgoritmo = new LinkedHashMap<>();

        String melhorAlgoritmo = "";
        double melhorTempo = Double.MAX_VALUE;
        double tempoEscolhido = 0;

        for (SortAlgorithm algoritmo : algoritmos) {
            List<ResultadoBenchmark> resultados = benchmarkRunner.runSerial(algoritmo, config);
            double media = BenchmarkStats.calcularMedia(resultados);

            temposPorAlgoritmo.put(algoritmo.getName(), media);

            if (media < melhorTempo) {
                melhorTempo = media;
                melhorAlgoritmo = algoritmo.getName();
            }

            if (algoritmo.getName().equals(algoritmoEscolhido)) {
                tempoEscolhido = media;
            }
        }

        int pontuacao = calcularPontuacao(tempoEscolhido, melhorTempo);

        return new ChallengeResult(
                algoritmoEscolhido,
                melhorAlgoritmo,
                tempoEscolhido,
                melhorTempo,
                pontuacao,
                temposPorAlgoritmo
        );
    }

    private int calcularPontuacao(double tempoEscolhido, double melhorTempo) {
        if (tempoEscolhido <= 0 || melhorTempo <= 0) {
            return 0;
        }

        double razao = melhorTempo / tempoEscolhido;

        int pontuacao = (int) Math.round(razao * 1000);

        if (pontuacao > 1000) {
            pontuacao = 1000;
        }

        return Math.max(pontuacao, 0);
    }
}