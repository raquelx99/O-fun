package ui;

import java.util.List;

public class AnalysisTextGenerator {

    public static String gerarAnalise(
            String tipoGrafico,
            List<ResumoResultado> resultadosFiltrados
    ) {
        if (resultadosFiltrados == null || resultadosFiltrados.isEmpty()) {
            return "Não há dados suficientes para gerar uma análise textual com os filtros selecionados.";
        }

        switch (tipoGrafico) {
            case "Tempo médio por algoritmo":
                return analisarTempoMedioPorAlgoritmo(resultadosFiltrados);

            case "Serial vs Paralelo":
                return analisarSerialVsParalelo(resultadosFiltrados);

            case "Speedup por threads":
                return analisarSpeedup(resultadosFiltrados);

            case "Eficiência por threads":
                return analisarEficiencia(resultadosFiltrados);

            case "Tempo por tamanho":
                return analisarTempoPorTamanho(resultadosFiltrados);

            case "Tempo por tipo de entrada":
                return analisarTempoPorTipoEntrada(resultadosFiltrados);

            default:
                return "Tipo de gráfico não reconhecido para análise textual.";
        }
    }

    private static String analisarTempoMedioPorAlgoritmo(List<ResumoResultado> resultados) {
        ResumoResultado melhor = encontrarMenorMedia(resultados);
        ResumoResultado pior = encontrarMaiorMedia(resultados);

        return "Análise do tempo médio por algoritmo:\n\n" +
                "Considerando os filtros selecionados, o algoritmo com menor tempo médio foi " +
                melhor.getAlgoritmo() + " (" + melhor.getVersao() + "), com média de " +
                formatar(melhor.getMediaMs()) + " ms. O maior tempo médio foi observado em " +
                pior.getAlgoritmo() + " (" + pior.getVersao() + "), com média de " +
                formatar(pior.getMediaMs()) + " ms.\n\n" +
                "Esse resultado permite comparar o impacto da escolha do algoritmo no desempenho. " +
                "Em geral, algoritmos como Quick Sort e Merge Sort tendem a apresentar melhor desempenho " +
                "em entradas maiores, enquanto Bubble Sort e Insertion Sort podem ser mais sensíveis ao tamanho " +
                "e à organização inicial dos dados.";
    }

    private static String analisarSerialVsParalelo(List<ResumoResultado> resultados) {
        double mediaSerial = calcularMediaPorVersao(resultados, "Serial");
        double mediaParalelo = calcularMediaPorVersao(resultados, "Paralelo");

        if (mediaSerial < 0 || mediaParalelo < 0) {
            return "Análise Serial vs Paralelo:\n\n" +
                    "Para realizar esta análise, é necessário que os filtros selecionados incluam execuções seriais e paralelas.";
        }

        String interpretacao;

        if (mediaParalelo < mediaSerial) {
            interpretacao = "A versão paralela apresentou menor tempo médio do que a versão serial, indicando ganho de desempenho com o uso de múltiplas threads neste cenário.";
        } else if (mediaParalelo > mediaSerial) {
            interpretacao = "A versão paralela apresentou maior tempo médio do que a versão serial, indicando que o custo de paralelização pode ter superado os ganhos neste cenário.";
        } else {
            interpretacao = "As versões serial e paralela apresentaram tempos médios muito próximos.";
        }

        return "Análise Serial vs Paralelo:\n\n" +
                "Tempo médio serial: " + formatar(mediaSerial) + " ms.\n" +
                "Tempo médio paralelo: " + formatar(mediaParalelo) + " ms.\n\n" +
                interpretacao + "\n\n" +
                "Esse comportamento é importante para a discussão do trabalho, pois demonstra que o paralelismo " +
                "não garante automaticamente melhor desempenho. Em entradas pequenas, por exemplo, o overhead de criação, " +
                "sincronização e mesclagem das tarefas pode tornar a versão paralela menos eficiente.";
    }

    private static String analisarSpeedup(List<ResumoResultado> resultados) {
        ResumoResultado melhor = encontrarMaiorSpeedup(resultados);

        if (melhor == null) {
            return "Análise de speedup:\n\n" +
                    "Não há resultados paralelos suficientes para calcular o speedup com os filtros selecionados.";
        }

        String interpretacao;

        if (melhor.getSpeedup() > 1.0) {
            interpretacao = "O speedup maior que 1 indica que a versão paralela foi mais rápida que a referência serial.";
        } else if (melhor.getSpeedup() < 1.0) {
            interpretacao = "O speedup menor que 1 indica que a versão paralela foi mais lenta que a versão serial.";
        } else {
            interpretacao = "O speedup próximo de 1 indica desempenho semelhante entre a versão serial e a paralela.";
        }

        return "Análise de speedup:\n\n" +
                "O maior speedup observado nos dados filtrados ocorreu em " +
                melhor.getAlgoritmo() + " com " + melhor.getThreads() + " threads, " +
                "obtendo speedup de " + formatar(melhor.getSpeedup()) + ".\n\n" +
                interpretacao + "\n\n" +
                "Essa métrica é relevante porque mostra a relação entre o tempo serial e o tempo paralelo. " +
                "Quanto maior o speedup, maior o ganho de desempenho obtido pela paralelização.";
    }

    private static String analisarEficiencia(List<ResumoResultado> resultados) {
        ResumoResultado melhor = encontrarMaiorEficiencia(resultados);

        if (melhor == null) {
            return "Análise de eficiência paralela:\n\n" +
                    "Não há resultados paralelos suficientes para calcular a eficiência com os filtros selecionados.";
        }

        return "Análise de eficiência paralela:\n\n" +
                "A maior eficiência observada ocorreu em " +
                melhor.getAlgoritmo() + " com " + melhor.getThreads() + " threads, " +
                "obtendo eficiência de " + formatar(melhor.getEficiencia()) + ".\n\n" +
                "A eficiência paralela relaciona o speedup obtido com a quantidade de threads utilizadas. " +
                "Quando a eficiência diminui com o aumento de threads, isso pode indicar overhead de paralelização, " +
                "custos de sincronização ou divisão desigual do trabalho entre as threads.";
    }

    private static String analisarTempoPorTamanho(List<ResumoResultado> resultados) {
        ResumoResultado menor = encontrarMenorMedia(resultados);
        ResumoResultado maior = encontrarMaiorMedia(resultados);

        return "Análise do impacto do tamanho da entrada:\n\n" +
                "Nos dados filtrados, o menor tempo médio foi observado para tamanho " +
                menor.getTamanhoEntrada() + ", com média de " + formatar(menor.getMediaMs()) + " ms. " +
                "O maior tempo médio foi observado para tamanho " + maior.getTamanhoEntrada() +
                ", com média de " + formatar(maior.getMediaMs()) + " ms.\n\n" +
                "Esse tipo de análise ajuda a observar como o crescimento da entrada afeta cada algoritmo. " +
                "Algoritmos de complexidade quadrática tendem a crescer mais rapidamente com o aumento do tamanho, " +
                "enquanto algoritmos como Quick Sort e Merge Sort costumam escalar melhor.";
    }

    private static String analisarTempoPorTipoEntrada(List<ResumoResultado> resultados) {
        ResumoResultado menor = encontrarMenorMedia(resultados);
        ResumoResultado maior = encontrarMaiorMedia(resultados);

        return "Análise do impacto do tipo de entrada:\n\n" +
                "O menor tempo médio nos dados filtrados ocorreu no tipo de entrada " +
                menor.getTipoEntrada() + ", com média de " + formatar(menor.getMediaMs()) + " ms. " +
                "O maior tempo médio ocorreu no tipo " + maior.getTipoEntrada() +
                ", com média de " + formatar(maior.getMediaMs()) + " ms.\n\n" +
                "Esse resultado mostra que a natureza dos dados influencia diretamente o desempenho. " +
                "Entradas ordenadas ou quase ordenadas podem favorecer algoritmos como Bubble Sort e Insertion Sort, " +
                "enquanto entradas invertidas ou aleatórias podem evidenciar diferenças mais fortes entre os métodos.";
    }

    private static ResumoResultado encontrarMenorMedia(List<ResumoResultado> resultados) {
        ResumoResultado melhor = resultados.get(0);

        for (ResumoResultado r : resultados) {
            if (r.getMediaMs() < melhor.getMediaMs()) {
                melhor = r;
            }
        }

        return melhor;
    }

    private static ResumoResultado encontrarMaiorMedia(List<ResumoResultado> resultados) {
        ResumoResultado pior = resultados.get(0);

        for (ResumoResultado r : resultados) {
            if (r.getMediaMs() > pior.getMediaMs()) {
                pior = r;
            }
        }

        return pior;
    }

    private static ResumoResultado encontrarMaiorSpeedup(List<ResumoResultado> resultados) {
        ResumoResultado melhor = null;

        for (ResumoResultado r : resultados) {
            if (!r.getVersao().equals("Paralelo")) {
                continue;
            }

            if (melhor == null || r.getSpeedup() > melhor.getSpeedup()) {
                melhor = r;
            }
        }

        return melhor;
    }

    private static ResumoResultado encontrarMaiorEficiencia(List<ResumoResultado> resultados) {
        ResumoResultado melhor = null;

        for (ResumoResultado r : resultados) {
            if (!r.getVersao().equals("Paralelo")) {
                continue;
            }

            if (melhor == null || r.getEficiencia() > melhor.getEficiencia()) {
                melhor = r;
            }
        }

        return melhor;
    }

    private static double calcularMediaPorVersao(List<ResumoResultado> resultados, String versao) {
        double soma = 0;
        int quantidade = 0;

        for (ResumoResultado r : resultados) {
            if (r.getVersao().equals(versao)) {
                soma += r.getMediaMs();
                quantidade++;
            }
        }

        if (quantidade == 0) {
            return -1;
        }

        return soma / quantidade;
    }

    private static String formatar(double valor) {
        return String.format("%.4f", valor);
    }
}