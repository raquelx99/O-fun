package parallel;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelMergeSort implements ParallelSortAlgorithm {

    @Override
    public String getName() {
        return "Merge Sort";
    }

    @Override
    public void sort(int[] array, int numberOfThreads) {
        if (array == null || array.length <= 1) {
            return;
        }

        int[] auxiliar = new int[array.length];

        try (ForkJoinPool pool = new ForkJoinPool(numberOfThreads)) {
            pool.invoke(new MergeSortTask(array, auxiliar, 0, array.length - 1));
        }
    }

    private static class MergeSortTask extends RecursiveAction {
        private static final int LIMITE_SEQUENCIAL = 10_000;

        private int[] array;
        private int[] auxiliar;
        private int inicio;
        private int fim;

        public MergeSortTask(int[] array, int[] auxiliar, int inicio, int fim) {
            this.array = array;
            this.auxiliar = auxiliar;
            this.inicio = inicio;
            this.fim = fim;
        }

        @Override
        protected void compute() {
            if (inicio >= fim) {
                return;
            }

            if (fim - inicio <= LIMITE_SEQUENCIAL) {
                mergeSortSequencial(array, auxiliar, inicio, fim);
                return;
            }

            int meio = (inicio + fim) / 2;

            MergeSortTask esquerda = new MergeSortTask(array, auxiliar, inicio, meio);
            MergeSortTask direita = new MergeSortTask(array, auxiliar, meio + 1, fim);

            invokeAll(esquerda, direita);

            mesclar(array, auxiliar, inicio, meio, fim);
        }

        private static void mergeSortSequencial(int[] array, int[] auxiliar, int inicio, int fim) {
            if (inicio >= fim) {
                return;
            }

            int meio = (inicio + fim) / 2;

            mergeSortSequencial(array, auxiliar, inicio, meio);
            mergeSortSequencial(array, auxiliar, meio + 1, fim);

            mesclar(array, auxiliar, inicio, meio, fim);
        }

        private static void mesclar(int[] array, int[] auxiliar, int inicio, int meio, int fim) {
            int i = inicio;
            int j = meio + 1;
            int k = inicio;

            while (i <= meio && j <= fim) {
                if (array[i] <= array[j]) {
                    auxiliar[k] = array[i];
                    i++;
                } else {
                    auxiliar[k] = array[j];
                    j++;
                }

                k++;
            }

            while (i <= meio) {
                auxiliar[k] = array[i];
                i++;
                k++;
            }

            while (j <= fim) {
                auxiliar[k] = array[j];
                j++;
                k++;
            }

            for (int x = inicio; x <= fim; x++) {
                array[x] = auxiliar[x];
            }
        }
    }
}