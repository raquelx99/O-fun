package parallel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sort.BubbleSort;

public class ParallelBubbleSort implements ParallelSortAlgorithm {

    @Override
    public String getName() {
        return "Bubble Sort";
    }

    @Override
    public void sort(int[] array, int numberOfThreads) {
        if (array == null || array.length <= 1) {
            return;
        }

        if (numberOfThreads <= 1) {
            new BubbleSort().sort(array);
            return;
        }

        int tamanho = array.length;
        int threadsReais = Math.min(numberOfThreads, tamanho);
        int tamanhoBloco = (int) Math.ceil((double) tamanho / threadsReais);

        ExecutorService executor = Executors.newFixedThreadPool(threadsReais);

        List<Callable<Void>> tarefas = new ArrayList<>();

        for (int i = 0; i < threadsReais; i++) {
            int inicio = i * tamanhoBloco;
            int fim = Math.min(inicio + tamanhoBloco, tamanho);

            if (inicio < fim) {
                tarefas.add(() -> {
                    int[] bloco = Arrays.copyOfRange(array, inicio, fim);
                    new BubbleSort().sort(bloco);
                    System.arraycopy(bloco, 0, array, inicio, bloco.length);
                    return null;
                });
            }
        }

        try {
            executor.invokeAll(tarefas);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Bubble Sort paralelo interrompido.");
        } finally {
            executor.shutdown();
        }

        mesclarBlocos(array, tamanhoBloco);
    }

    private void mesclarBlocos(int[] array, int tamanhoBloco) {
        int tamanhoAtual = tamanhoBloco;

        while (tamanhoAtual < array.length) {
            for (int inicio = 0; inicio < array.length; inicio += 2 * tamanhoAtual) {
                int meio = Math.min(inicio + tamanhoAtual, array.length);
                int fim = Math.min(inicio + 2 * tamanhoAtual, array.length);

                if (meio < fim) {
                    mesclar(array, inicio, meio, fim);
                }
            }

            tamanhoAtual *= 2;
        }
    }

    private void mesclar(int[] array, int inicio, int meio, int fim) {
        int[] temp = new int[fim - inicio];

        int i = inicio;
        int j = meio;
        int k = 0;

        while (i < meio && j < fim) {
            if (array[i] <= array[j]) {
                temp[k] = array[i];
                i++;
            } else {
                temp[k] = array[j];
                j++;
            }

            k++;
        }

        while (i < meio) {
            temp[k] = array[i];
            i++;
            k++;
        }

        while (j < fim) {
            temp[k] = array[j];
            j++;
            k++;
        }

        for (int x = 0; x < temp.length; x++) {
            array[inicio + x] = temp[x];
        }
    }
}