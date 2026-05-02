package sort;

public class QuickSort implements SortAlgorithm {

    @Override
    public String getName() {
        return "Quick Sort";
    }

    @Override
    public void sort(int[] array) {
        if (array == null || array.length <= 1) return;
        quickSort(array, 0, array.length - 1);
    }

    private void quickSort(int[] array, int inicio, int fim) {
        if (inicio < fim) {
            int indicePivo = particionar(array, inicio, fim);
            quickSort(array, inicio, indicePivo - 1);
            quickSort(array, indicePivo + 1, fim);
        }
    }

    private int particionar(int[] array, int inicio, int fim) {
        medianaDeTres(array, inicio, fim);
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

    public static void medianaDeTres(int[] array, int inicio, int fim) {
        int mid = inicio + (fim - inicio) / 2;

        if (array[inicio] > array[mid])  trocar(array, inicio, mid);
        if (array[inicio] > array[fim])  trocar(array, inicio, fim);
        if (array[mid]    > array[fim])  trocar(array, mid,    fim);

        trocar(array, mid, fim);
    }

    public static void trocar(int[] array, int a, int b) {
        int temp  = array[a];
        array[a]  = array[b];
        array[b]  = temp;
    }
}
