package benchmark;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import model.ResultadoBenchmark;

public class CsvExporter {

    public static void writeHeader(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("algoritmo,versao,threads,tamanhoEntrada,tipoEntrada,amostra,tempoMs,score,ordenadoCorretamente");
        } catch (IOException e) {
            System.out.println("Erro ao criar CSV: " + e.getMessage());
        }
    }

    public static void appendResults(String filePath, List<ResultadoBenchmark> resultados) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
            for (ResultadoBenchmark resultado : resultados) {
                writer.println(resultado.toCsvLine());
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar CSV: " + e.getMessage());
        }
    }
}