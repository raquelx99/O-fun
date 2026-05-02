package benchmark;

import java.util.ArrayList;
import java.util.List;

import game.ScoreCalculator;
import model.ResultadoBenchmark;
import parallel.ParallelSortAlgorithm;
import sort.SortAlgorithm;

public class BenchmarkRunner {

    public List<ResultadoBenchmark> runSerial(
            SortAlgorithm algoritmo,
            BenchmarkConfig config
    ) {
        List<ResultadoBenchmark> resultados = new ArrayList<>();

        for (int amostra = 1; amostra <= config.getAmostras(); amostra++) {
            int[] array = DatasetGenerator.generate(
                    config.getTamanhoEntrada(),
                    config.getTipoEntrada()
            );

            long inicio = System.nanoTime();

            algoritmo.sort(array);

            long fim = System.nanoTime();

            if (!estaOrdenado(array)) {
                System.out.println("Erro: array não foi ordenado corretamente.");
            }

            double tempoMs = (fim - inicio) / 1_000_000.0;
            double score = ScoreCalculator.calculate(config.getTamanhoEntrada(), tempoMs);

            ResultadoBenchmark resultado = new ResultadoBenchmark(
                    algoritmo.getName(),
                    "Serial",
                    1,
                    config.getTamanhoEntrada(),
                    config.getTipoEntrada().name(),
                    amostra,
                    tempoMs,
                    score
            );

            resultados.add(resultado);
        }

        return resultados;
    }

    public List<ResultadoBenchmark> runParallel(
            ParallelSortAlgorithm algoritmo,
            BenchmarkConfig config,
            int numberOfThreads
    ) {
        List<ResultadoBenchmark> resultados = new ArrayList<>();

        for (int amostra = 1; amostra <= config.getAmostras(); amostra++) {
            int[] array = DatasetGenerator.generate(
                    config.getTamanhoEntrada(),
                    config.getTipoEntrada()
            );

            long inicio = System.nanoTime();

            algoritmo.sort(array, numberOfThreads);

            long fim = System.nanoTime();

            if (!estaOrdenado(array)) {
                System.out.println("Erro: array não foi ordenado corretamente.");
            }

            double tempoMs = (fim - inicio) / 1_000_000.0;
            double score = ScoreCalculator.calculate(config.getTamanhoEntrada(), tempoMs);

            ResultadoBenchmark resultado = new ResultadoBenchmark(
                    algoritmo.getName(),
                    "Paralelo",
                    numberOfThreads,
                    config.getTamanhoEntrada(),
                    config.getTipoEntrada().name(),
                    amostra,
                    tempoMs,
                    score
            );

            resultados.add(resultado);
        }

        return resultados;
    }

    private boolean estaOrdenado(int[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] > array[i]) {
                return false;
            }
        }

        return true;
    }
}