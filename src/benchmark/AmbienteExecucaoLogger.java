package benchmark;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class AmbienteExecucaoLogger {

    public static void salvar(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("Informações do Ambiente de Execução");
            writer.println("-----------------------------------");
            writer.println("Sistema operacional: " + System.getProperty("os.name"));
            writer.println("Versão do sistema: " + System.getProperty("os.version"));
            writer.println("Arquitetura: " + System.getProperty("os.arch"));
            writer.println("Versão do Java: " + System.getProperty("java.version"));
            writer.println("Fornecedor do Java: " + System.getProperty("java.vendor"));
            writer.println("Núcleos/processadores disponíveis: " + Runtime.getRuntime().availableProcessors());
            writer.println("Memória máxima da JVM (MB): " + Runtime.getRuntime().maxMemory() / (1024 * 1024));
        } catch (IOException e) {
            System.out.println("Erro ao salvar informações do ambiente: " + e.getMessage());
        }
    }
}