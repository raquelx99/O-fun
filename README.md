# O(fun) — Porque O(n²) é passado!

> Análise comparativa de algoritmos de ordenação em ambientes seriais e paralelos, com interface gamificada em Java Swing.

Trabalho acadêmico desenvolvido para a disciplina de Computação Paralela e Concorrente — **Universidade de Fortaleza (UNIFOR)**, Curso de Ciência da Computação.

---

## Sobre o projeto

O **O(fun)** implementa quatro algoritmos de ordenação em versões serial e paralela, mede seu desempenho em diferentes cenários e apresenta os resultados por meio de uma interface gráfica interativa e gamificada. O objetivo é investigar como fatores como tamanho da entrada, natureza dos dados e número de threads influenciam o desempenho dos algoritmos.

---

## Algoritmos implementados

| Algoritmo | Serial | Paralelo | Estratégia paralela |
|---|---|---|---|
| Bubble Sort | ✓ | ✓ | Particionamento em blocos + merge |
| Insertion Sort | ✓ | ✓ | Particionamento em blocos + merge |
| Quick Sort | ✓ | ✓ | ForkJoinPool (divisão e conquista) |
| Merge Sort | ✓ | ✓ | ForkJoinPool (divisão e conquista) |

> **Quick Sort** usa pivô **mediana-de-três** para evitar degeneração O(n²) em entradas ordenadas ou invertidas.

---

## Funcionalidades da interface

### Laboratório de Benchmark
Configure e execute benchmarks personalizados: escolha o algoritmo, versão (serial/paralelo), número de threads, tamanho da entrada, tipo de entrada e número de amostras. Visualize os resultados em tempo real.

### Modo Desafio
Escolha o algoritmo mais eficiente para cada cenário temático e acumule pontos. A interface compara seu desempenho com o algoritmo ótimo e exibe a pontuação animada.

### Modo Aprenda
Modo didático com dois submodos:
- **Livre** — arraste blocos coloridos para ordená-los manualmente, com indicador visual de peso
- **Guiado** — siga o algoritmo passo a passo, executando cada comparação e troca com feedback visual imediato de acerto/erro

### Resultados e Gráficos
Visualize e exporte os dados coletados em 11 tipos de gráfico:
- Tempo médio por algoritmo
- Serial vs Paralelo
- Speedup por threads
- Eficiência por threads
- Tempo por tamanho da entrada
- Tempo por tipo de entrada
- Speedup por algoritmo (barras agrupadas)
- Eficiência por algoritmo (barras agrupadas)
- Tempo por tipo de entrada por algoritmo (barras agrupadas)
- Curva de crescimento — todos os algoritmos (linhas)
- Serial vs Paralelo 8T por algoritmo (linhas)

Todos os gráficos podem ser **exportados como PNG** em alta resolução (1400×700px).

---

## Métricas coletadas

- Tempo de execução por amostra (ms)
- Tempo médio, menor e maior tempo
- Speedup: `Speedup = Tempo Serial / Tempo Paralelo`
- Eficiência paralela: `Eficiência = Speedup / Número de Threads`
- Validação da ordenação (OK / ERRO)

---

## Tipos de entrada

| Tipo | Descrição |
|---|---|
| ALEATORIA | Elementos em ordem aleatória |
| ORDENADA | Elementos já em ordem crescente |
| QUASE_ORDENADA | Ordenada com pequenas perturbações |
| INVERTIDA | Elementos em ordem decrescente |
| REPETIDA | Muitos valores duplicados |

Todos os datasets usam **seed fixa (42)** para garantir reprodutibilidade dos experimentos.

---

## Como executar

### Pré-requisitos

- **Java 21** ou superior
- IDE com suporte a Java (IntelliJ IDEA, Eclipse ou VS Code com extensão Java)

### Compilar e executar

```bash
# Na raiz do projeto
javac -d bin -sourcepath src $(find src -name "*.java")
java -cp bin app.Main
```

> **Importante:** execute sempre a partir da **raiz do projeto** para que a pasta `fonts/` seja encontrada corretamente.

### Via IDE

Importe o projeto como projeto Java simples, defina `src/` como source root e execute `app.Main`.

---

## Saídas geradas

| Arquivo | Conteúdo |
|---|---|
| `resultados_sorts.csv` | Resultados brutos por amostra |
| `resultados_resumo.csv` | Resumo estatístico por configuração |
| `ambiente_execucao.txt` | Informações do hardware e JVM |

---

## Tecnologias utilizadas

- **Java 21** — linguagem principal
- **Java Swing** — interface gráfica
- **ForkJoinPool** — paralelismo de Quick Sort e Merge Sort
- **ExecutorService + Callable** — paralelismo de Bubble Sort e Insertion Sort
- **ImageIO** — exportação de gráficos em PNG
- **Poppins** (Google Fonts) — tipografia da interface

---

## Autores

- **Raquel Albuquerque Quirino**
- **João Igor Vidal de Andrade**

Universidade de Fortaleza — UNIFOR  
Centro de Ciências Tecnológicas — Ciência da Computação
