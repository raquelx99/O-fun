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
        if (!arquivo.exists()) return resultados;

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha = reader.readLine();

            while ((linha = reader.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;

                String[] partes = splitCsv(linha);

                if (partes.length < 11) continue;

                try {
                    resultados.add(new ResumoResultado(
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
                    ));
                } catch (NumberFormatException e) {
                    System.out.println("Linha ignorada (formato invalido): " + linha);
                }
            }

        } catch (Exception e) {
            System.out.println("Erro ao ler CSV de resumo: " + e.getMessage());
        }

        return resultados;
    }

    private static String[] splitCsv(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean dentroAspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                dentroAspas = !dentroAspas;
            } else if (c == ',' && !dentroAspas) {
                campos.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        campos.add(sb.toString().trim());

        return campos.toArray(new String[0]);
    }
}
