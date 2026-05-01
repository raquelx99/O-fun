package sort;

public class MergeSort implements SortAlgorithm {

    @Override
    public String getName() {
        return "Merge Sort";
    }

    @Override
    public void sort(int[] array) {
        if (array.length <= 1) {
            return;
        }

        int[] auxiliar = new int[array.length];
        mergeSort(array, auxiliar, 0, array.length - 1);
    }

    private void mergeSort(int[] array, int[] auxiliar, int inicio, int fim) {
        if (inicio >= fim) {
            return;
        }

        int meio = (inicio + fim) / 2;

        mergeSort(array, auxiliar, inicio, meio);
        mergeSort(array, auxiliar, meio + 1, fim);

        mesclar(array, auxiliar, inicio, meio, fim);
    }

    private void mesclar(int[] array, int[] auxiliar, int inicio, int meio, int fim) {
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