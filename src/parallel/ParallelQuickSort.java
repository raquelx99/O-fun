package parallel;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelQuickSort implements ParallelSortAlgorithm {

    @Override
    public String getName() {
        return "Quick Sort";
    }

    @Override
    public void sort(int[] array, int numberOfThreads) {
        if (array == null || array.length <= 1) {
            return;
        }

        try (ForkJoinPool pool = new ForkJoinPool(numberOfThreads)) {
            pool.invoke(new QuickSortTask(array, 0, array.length - 1));
            pool.shutdown();
        }
    }

    private static class QuickSortTask extends RecursiveAction {
        private static final int LIMITE_SEQUENCIAL = 10_000;

        private int[] array;
        private int inicio;
        private int fim;

        public QuickSortTask(int[] array, int inicio, int fim) {
            this.array = array;
            this.inicio = inicio;
            this.fim = fim;
        }

        @Override
        protected void compute() {
            if (inicio >= fim) {
                return;
            }

            if (fim - inicio <= LIMITE_SEQUENCIAL) {
                quickSortSequencial(array, inicio, fim);
                return;
            }

            int indicePivo = particionar(array, inicio, fim);

            QuickSortTask esquerda = new QuickSortTask(array, inicio, indicePivo - 1);
            QuickSortTask direita = new QuickSortTask(array, indicePivo + 1, fim);

            invokeAll(esquerda, direita);
        }

        private static void quickSortSequencial(int[] array, int inicio, int fim) {
            if (inicio < fim) {
                int indicePivo = particionar(array, inicio, fim);

                quickSortSequencial(array, inicio, indicePivo - 1);
                quickSortSequencial(array, indicePivo + 1, fim);
            }
        }

        private static int particionar(int[] array, int inicio, int fim) {
            int pivo = array[fim];
            int i = inicio - 1;

            for (int j = inicio; j < fim; j++) {
                if (array[j] <= pivo) {
                    i++;
                    trocar(array, i, j);
                }
            }

            trocar(array, i + 1, fim);
            return i + 1;
        }

        private static void trocar(int[] array, int a, int b) {
            int temp = array[a];
            array[a] = array[b];
            array[b] = temp;
        }
    }
}