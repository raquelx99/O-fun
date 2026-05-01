package app;

import java.util.List;

import benchmark.BenchmarkConfig;
import benchmark.BenchmarkRunner;
import benchmark.CsvExporter;
import model.ResultadoBenchmark;
import model.TipoEntrada;
import sort.BubbleSort;
import sort.InsertionSort;
import sort.MergeSort;
import sort.QuickSort;
import sort.SortAlgorithm;

public class Main {

    public static void main(String[] args) {
        String arquivoCsv = "resultados.csv";

        CsvExporter.writeHeader(arquivoCsv);

        BenchmarkConfig config = new BenchmarkConfig(
                10000,
                TipoEntrada.ALEATORIA,
                5
        );

        BenchmarkRunner runner = new BenchmarkRunner();

        SortAlgorithm[] algoritmos = {
                new BubbleSort(),
                new InsertionSort(),
                new QuickSort(),
                new MergeSort()
        };

        for (SortAlgorithm algoritmo : algoritmos) {
            System.out.println("Executando: " + algoritmo.getName());

            List<ResultadoBenchmark> resultados = runner.runSerial(algoritmo, config);

            for (ResultadoBenchmark resultado : resultados) {
                System.out.println(
                        resultado.getAlgoritmo() +
                                " | Amostra " + resultado.getAmostra() +
                                " | Tempo: " + resultado.getTempoMs() + " ms" +
                                " | Score: " + resultado.getScore()
                );
            }

            CsvExporter.appendResults(arquivoCsv, resultados);

            System.out.println("----------------------------------");
        }

        System.out.println("Benchmark finalizado!");
        System.out.println("Resultados salvos em: " + arquivoCsv);
    }
}