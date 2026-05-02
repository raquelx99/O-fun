package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import benchmark.AmbienteExecucaoLogger;
import benchmark.BenchmarkConfig;
import benchmark.BenchmarkRunner;
import benchmark.BenchmarkStats;
import benchmark.CsvExporter;
import benchmark.CsvResumoExporter;
import model.ResultadoBenchmark;
import model.TipoEntrada;
import parallel.ParallelBubbleSort;
import parallel.ParallelInsertionSort;
import parallel.ParallelMergeSort;
import parallel.ParallelQuickSort;
import parallel.ParallelSortAlgorithm;
import sort.BubbleSort;
import sort.InsertionSort;
import sort.MergeSort;
import sort.QuickSort;
import sort.SortAlgorithm;

public class Main {

    public static void main(String[] args) {
        String arquivoCsvBruto = "resultados_sorts.csv";
        String arquivoCsvResumo = "resultados_resumo.csv";
        String arquivoAmbiente = "ambiente_execucao.txt";

        CsvExporter.writeHeader(arquivoCsvBruto);
        CsvResumoExporter.writeHeader(arquivoCsvResumo);
        AmbienteExecucaoLogger.salvar(arquivoAmbiente);

        BenchmarkRunner runner = new BenchmarkRunner();

        List<SortAlgorithm> algoritmosSeriais = new ArrayList<>();
        algoritmosSeriais.add(new BubbleSort());
        algoritmosSeriais.add(new InsertionSort());
        algoritmosSeriais.add(new QuickSort());
        algoritmosSeriais.add(new MergeSort());

        List<ParallelSortAlgorithm> algoritmosParalelos = new ArrayList<>();
        algoritmosParalelos.add(new ParallelBubbleSort());
        algoritmosParalelos.add(new ParallelInsertionSort());
        algoritmosParalelos.add(new ParallelQuickSort());
        algoritmosParalelos.add(new ParallelMergeSort());

        TipoEntrada[] tiposEntrada = {
                TipoEntrada.ALEATORIA,
                TipoEntrada.ORDENADA,
                TipoEntrada.QUASE_ORDENADA,
                TipoEntrada.INVERTIDA,
                TipoEntrada.REPETIDA
        };

        int[] tamanhos = {
                1000,
                5000,
                10000
        };

        int[] quantidadesThreads = {
                1,
                2,
                4,
                8
        };

        Map<String, Double> mediasSeriais = new HashMap<>();

        for (TipoEntrada tipoEntrada : tiposEntrada) {
            for (int tamanho : tamanhos) {
                BenchmarkConfig config = new BenchmarkConfig(
                        tamanho,
                        tipoEntrada,
                        5
                );

                System.out.println("=====================================");
                System.out.println("Tipo de entrada: " + tipoEntrada);
                System.out.println("Tamanho: " + tamanho);
                System.out.println("=====================================");

                executarSeriais(
                        runner,
                        algoritmosSeriais,
                        config,
                        arquivoCsvBruto,
                        arquivoCsvResumo,
                        mediasSeriais
                );

                executarParalelos(
                        runner,
                        algoritmosParalelos,
                        config,
                        quantidadesThreads,
                        arquivoCsvBruto,
                        arquivoCsvResumo,
                        mediasSeriais
                );
            }
        }

        System.out.println("Benchmark finalizado!");
        System.out.println("CSV bruto salvo em: " + arquivoCsvBruto);
        System.out.println("CSV de resumo salvo em: " + arquivoCsvResumo);
        System.out.println("Ambiente de execução salvo em: " + arquivoAmbiente);
    }

    private static void executarSeriais(
            BenchmarkRunner runner,
            List<SortAlgorithm> algoritmosSeriais,
            BenchmarkConfig config,
            String arquivoCsvBruto,
            String arquivoCsvResumo,
            Map<String, Double> mediasSeriais
    ) {
        for (SortAlgorithm algoritmo : algoritmosSeriais) {
            System.out.println("Executando serial: " + algoritmo.getName());

            List<ResultadoBenchmark> resultados = runner.runSerial(algoritmo, config);

            imprimirResumo(resultados);

            CsvExporter.appendResults(arquivoCsvBruto, resultados);

            double mediaSerial = BenchmarkStats.calcularMedia(resultados);

            String chave = criarChave(
                    algoritmo.getName(),
                    config.getTamanhoEntrada(),
                    config.getTipoEntrada().name()
            );

            mediasSeriais.put(chave, mediaSerial);

            CsvResumoExporter.appendResumo(
                    arquivoCsvResumo,
                    resultados,
                    mediaSerial
            );
        }
    }

    private static void executarParalelos(
            BenchmarkRunner runner,
            List<ParallelSortAlgorithm> algoritmosParalelos,
            BenchmarkConfig config,
            int[] quantidadesThreads,
            String arquivoCsvBruto,
            String arquivoCsvResumo,
            Map<String, Double> mediasSeriais
    ) {
        for (ParallelSortAlgorithm algoritmo : algoritmosParalelos) {
            for (int threads : quantidadesThreads) {
                System.out.println("Executando paralelo: " + algoritmo.getName() + " com " + threads + " threads");

                List<ResultadoBenchmark> resultados = runner.runParallel(
                        algoritmo,
                        config,
                        threads
                );

                imprimirResumo(resultados);

                CsvExporter.appendResults(arquivoCsvBruto, resultados);

                String chave = criarChave(
                        algoritmo.getName(),
                        config.getTamanhoEntrada(),
                        config.getTipoEntrada().name()
                );

                double mediaSerialReferencia = mediasSeriais.getOrDefault(chave, 0.0);

                CsvResumoExporter.appendResumo(
                        arquivoCsvResumo,
                        resultados,
                        mediaSerialReferencia
                );
            }
        }
    }

    private static String criarChave(String algoritmo, int tamanhoEntrada, String tipoEntrada) {
        return algoritmo + "|" + tamanhoEntrada + "|" + tipoEntrada;
    }

    private static void imprimirResumo(List<ResultadoBenchmark> resultados) {
        if (resultados.isEmpty()) {
            return;
        }

        ResultadoBenchmark primeiro = resultados.get(0);

        System.out.println(
                primeiro.getAlgoritmo() +
                        " | " + primeiro.getVersao() +
                        " | Threads: " + primeiro.getThreads() +
                        " | Tamanho: " + primeiro.getTamanhoEntrada() +
                        " | Tipo: " + primeiro.getTipoEntrada()
        );

        System.out.println("Média: " + BenchmarkStats.calcularMedia(resultados) + " ms");
        System.out.println("Menor: " + BenchmarkStats.calcularMenorTempo(resultados) + " ms");
        System.out.println("Maior: " + BenchmarkStats.calcularMaiorTempo(resultados) + " ms");
        System.out.println("Todos ordenados corretamente? " + BenchmarkStats.todosOrdenadosCorretamente(resultados));
        System.out.println("----------------------------------");
    }
}