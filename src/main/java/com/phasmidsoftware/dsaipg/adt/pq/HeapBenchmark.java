package com.phasmidsoftware.dsaipg.adt.pq;

import com.phasmidsoftware.dsaipg.adt.threesum.Source;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class HeapBenchmark {
    private static final int N_RUNS = 1000;
    private static final int M = 4095;
    private static final int N_INSERTS = 16000;
    private static final int N_REMOVES = 4000;
    private static final long seed = 10;
    private static final Random random = new Random(seed);

    private static <T> long[] testHeap(Class<? extends PriorityQueue<T>> clazz, boolean floyd, int M, Integer[] a) throws Exception {
        PriorityQueue<Integer> heap =  (PriorityQueue<Integer>) clazz
                .getDeclaredConstructor(int.class, boolean.class, Comparator.class, boolean.class)
                .newInstance(M, true, Comparator.naturalOrder(), floyd);

        long start = System.nanoTime();
        for (int i = 0; i < N_INSERTS; i++) heap.give(a[i]);
        long insertTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < N_REMOVES; i++) heap.take();
        long removeTime = System.nanoTime() - start;

        return new long[]{insertTime, removeTime, heap.cnt_compares, heap.cnt_swaps};
    }

    private static Integer[] shuffleArray(Integer[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Integer temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
        return array;
    }

    public static void main(String[] args) throws Exception {
        double[] avgInsertTimes = new double[4];
        double[] avgRemoveTimes = new double[4];
        long[] totalCompares = new long[4];
        long[] totalSwaps = new long[4];
        Class<? extends PriorityQueue<Integer>>[] heapTypes = new Class[]{
                PriorityQueue.class, PriorityQueue.class, FouraryHeap.class, FouraryHeap.class
        };
        boolean[] floydFlags = {false, true, false, true};

        Source source = new Source(N_INSERTS, N_INSERTS, random);
        Integer[] inputArray = Arrays.stream(source.intsSupplier(10).get())
                .boxed().toArray(Integer[]::new);

        for (int i = 0; i < N_RUNS; i++) {
            shuffleArray(inputArray);
            for (int j = 0; j < 4; j++) {
                long[] times = testHeap(heapTypes[j], floydFlags[j], M, inputArray.clone());
                avgInsertTimes[j] += times[0];
                avgRemoveTimes[j] += times[1];
                totalCompares[j] += times[2];
                totalSwaps[j] += times[3];
            }
        }

        // Convert total time to average milliseconds
        for (int i = 0; i < 4; i++) {
            avgInsertTimes[i] /= (N_RUNS * 1e6);
            avgRemoveTimes[i] /= (N_RUNS * 1e6);
        }

        System.out.println("For n = " + N_INSERTS + ", M = " + M);
        for (int i = 0; i < 4; i++) {
            System.out.printf("Heap %d (Floyd=%s) - Insert: %.3f ms, Remove: %.3f ms%n, Compares: %d, Swaps: %d%n",
                    i + 1, floydFlags[i], avgInsertTimes[i], avgRemoveTimes[i], totalCompares[i], totalSwaps[i]);
        }
    }
}
