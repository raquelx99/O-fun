package parallel;

public interface ParallelSortAlgorithm {
    String getName();

    void sort(int[] array, int numberOfThreads);
}