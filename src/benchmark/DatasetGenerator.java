package benchmark;

import java.util.Random;
import model.TipoEntrada;

public class DatasetGenerator {

    private static final long SEED = 42L;

    public static int[] generate(int size, TipoEntrada tipo) {
        switch (tipo) {
            case ALEATORIA:
                return generateRandom(size);

            case ORDENADA:
                return generateSorted(size);

            case QUASE_ORDENADA:
                return generateNearlySorted(size);

            case INVERTIDA:
                return generateReversed(size);

            case REPETIDA:
                return generateRepeated(size);

            default:
                return generateRandom(size);
        }
    }

    private static int[] generateRandom(int size) {
        Random random = new Random(SEED);
        int[] array = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(size);
        }

        return array;
    }

    private static int[] generateSorted(int size) {
        int[] array = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = i;
        }

        return array;
    }

    private static int[] generateNearlySorted(int size) {
        int[] array = generateSorted(size);
        Random random = new Random(SEED);

        int quantidadeTrocas = Math.max(1, size / 20);

        for (int i = 0; i < quantidadeTrocas; i++) {
            int a = random.nextInt(size);
            int b = random.nextInt(size);

            int temp = array[a];
            array[a] = array[b];
            array[b] = temp;
        }

        return array;
    }

    private static int[] generateReversed(int size) {
        int[] array = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = size - i;
        }

        return array;
    }

    private static int[] generateRepeated(int size) {
        Random random = new Random(SEED);
        int[] array = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(101);
        }

        return array;
    }
}