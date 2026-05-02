package ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ResumoCsvReader {

    public static List<ResumoResultado> ler(String caminhoArquivo) {
        List<ResumoResultado> resultados = new ArrayList<>();

        File arquivo = new File(caminhoArquivo);

        if (!arquivo.exists()) {
            return resultados;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha = reader.readLine();

            while ((linha = reader.readLine()) != null) {
                if (linha.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linha.split(",");

                if (partes.length < 11) {
                    continue;
                }

                ResumoResultado resultado = new ResumoResultado(
                        partes[0],
                        partes[1],
                        Integer.parseInt(partes[2]),
                        Integer.parseInt(partes[3]),
                        partes[4],
                        Double.parseDouble(partes[5]),
                        Double.parseDouble(partes[6]),
                        Double.parseDouble(partes[7]),
                        Double.parseDouble(partes[8]),
                        Double.parseDouble(partes[9]),
                        Boolean.parseBoolean(partes[10])
                );

                resultados.add(resultado);
            }

        } catch (Exception e) {
            System.out.println("Erro ao ler CSV de resumo: " + e.getMessage());
        }

        return resultados;
    }
}