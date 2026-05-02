package benchmark;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import model.ResultadoBenchmark;

public class CsvResumoExporter {

    public static void writeHeader(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("algoritmo,versao,threads,tamanhoEntrada,tipoEntrada,mediaMs,menorMs,maiorMs,speedup,eficiencia,todosOrdenadosCorretamente");
        } catch (IOException e) {
            System.out.println("Erro ao criar CSV de resumo: " + e.getMessage());
        }
    }

    public static void appendResumo(
            String filePath,
            List<ResultadoBenchmark> resultados,
            double tempoSerialReferencia
    ) {
        if (resultados == null || resultados.isEmpty()) {
            return;
        }

        ResultadoBenchmark primeiro = resultados.get(0);

        double media = BenchmarkStats.calcularMedia(resultados);
        double menor = BenchmarkStats.calcularMenorTempo(resultados);
        double maior = BenchmarkStats.calcularMaiorTempo(resultados);

        double speedup = 1.0;
        double eficiencia = 1.0;

        if (primeiro.getVersao().equalsIgnoreCase("Paralelo") && tempoSerialReferencia > 0) {
            speedup = tempoSerialReferencia / media;
            eficiencia = speedup / primeiro.getThreads();
        }

        boolean todosOrdenados = BenchmarkStats.todosOrdenadosCorretamente(resultados);

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
            writer.println(
                    "\"" + primeiro.getAlgoritmo() + "\"" + "," +
                            "\"" + primeiro.getVersao() + "\"" + "," +
                            primeiro.getThreads() + "," +
                            primeiro.getTamanhoEntrada() + "," +
                            "\"" + primeiro.getTipoEntrada() + "\"" + "," +
                            media + "," +
                            menor + "," +
                            maior + "," +
                            speedup + "," +
                            eficiencia + "," +
                            todosOrdenados
            );
        } catch (IOException e) {
            System.out.println("Erro ao salvar CSV de resumo: " + e.getMessage());
        }
    }
}