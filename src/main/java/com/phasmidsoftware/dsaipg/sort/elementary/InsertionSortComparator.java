/*
 * Copyright (c) 2024. Robin Hillyard
 */
package com.phasmidsoftware.dsaipg.sort.elementary;

import com.phasmidsoftware.dsaipg.adt.threesum.Source;
import com.phasmidsoftware.dsaipg.sort.Helper;
import com.phasmidsoftware.dsaipg.sort.Sort;
import com.phasmidsoftware.dsaipg.sort.SortWithHelper;
import com.phasmidsoftware.dsaipg.util.Config;
import com.phasmidsoftware.dsaipg.util.Config_Benchmark;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.IntStream;

import static com.phasmidsoftware.dsaipg.sort.InstrumentedComparatorHelper.getRunsConfig;

/**
 * A class for performing insertion sort using a comparator, extending functionality from SortWithHelper.
 * This includes methods for initialization and invocation of insertion sort,
 * along with specific utilities like counting inversions.
 *
 * @param <X> the type of elements to be sorted, which can be compared using a provided comparator.
 */
public class InsertionSortComparator<X> extends SortWithHelper<X> {
    /**
     * Constructor for InsertionSortComparator, which initializes the comparator with the provided helper.
     *
     * @param helper the Helper object to be used for managing the sorting process.
     */
    public InsertionSortComparator(Helper<X> helper) {
        super(helper);
    }

    /**
     * Constructor for any subclasses to use.
     *
     * @param description the description.
     * @param comparator  the comparator to use.
     * @param N           the number of elements expected.
     * @param nRuns       the number of runs to be expected (this is only significant when instrumenting).
     * @param config      the configuration.
     */
    protected InsertionSortComparator(String description, Comparator<X> comparator, int N, int nRuns, Config config) {
        super(description, comparator, N, nRuns, config);
    }

    /**
     * Constructor for InsertionSort
     *
     * @param N      the number elements we expect to sort.
     * @param nRuns  the number of runs to be expected (this is only significant when instrumenting).
     * @param config the configuration.
     */
    public InsertionSortComparator(Comparator<X> comparator, int N, int nRuns, Config config) {
        this(DESCRIPTION, comparator, N, nRuns, config);
    }

    /**
     * Sort the sub-array xs:from:to using insertion sort.
     *
     * @param xs   sort the array xs from "from" to "to".
     * @param from the index of the first element to sort
     * @param to   the index of the first element not to sort
     */
    public void sort(X[] xs, int from, int to) {
        final Helper<X> helper = getHelper();
        for (int i = from; i < to - 1; i++) {
            int smallest = i;
            for (int j = i; j < to; j++) {
                if (helper.compare(xs, smallest, j) > 0) {
                    smallest = j;
                }
            }
            if (smallest != i) {
                helper.swap(xs, smallest, i);
            }
        }
    }

    public static final String DESCRIPTION = "Insertion sort";

    /**
     * Sorts the given array in-place using the provided insertion sort comparator.
     *
     * @param <T> the generic type parameter that extends Comparable.
     * @param ts  the array of elements to be sorted, where elements must implement {@code Comparable}.
     *            The method modifies this array directly to produce the sorted order.
     * @throws RuntimeException if an IOException occurs during the sorting process.
     */
    public static <T extends Comparable<T>> void sort(T[] ts) {
        try (InsertionSortComparator<T> sort = new InsertionSortComparator<>(DESCRIPTION, Comparable::compareTo, ts.length, 1, Config.load(InsertionSortComparator.class))) {
            sort.mutatingSort(ts);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a case-insensitive string sorter using an insertion sort comparator.
     *
     * @param n      the expected number of elements to be sorted.
     * @param config the configuration object containing necessary settings.
     * @return a {@code SortWithHelper<String>} instance configured for case-insensitive string sorting.
     */
    public static Sort<String> stringSorterCaseInsensitive(int n, Config config) {
        return new InsertionSortComparator<>(DESCRIPTION, String.CASE_INSENSITIVE_ORDER, n, getRunsConfig(config), config);
    }

    /**
     * This method is designed to count inversions in quadratic time, using insertion sort.
     *
     * @param ts  an array of comparable T elements.
     * @param <T> the underlying type of the elements.
     * @return the number of inversions in ts, which remains unchanged.
     */
    public static <T> long countInversions(T[] ts, Comparator<T> comparator) {
        final Config config = Config_Benchmark.setupConfigFixes();
        try (InsertionSortComparator<T> sorter = new InsertionSortComparator<>(comparator, ts.length, getRunsConfig(config), config)) {
            Helper<T> helper = sorter.getHelper();
            sorter.sort(ts, true);
            return helper.getFixes();
        }
    }

    public static void main(String[] args) {
        int[] ns = {2500, 5000, 10000, 20000, 40000};
        int runs = getRunsConfig(Config_Benchmark.setupConfigFixes());
        double partialSortStart = 0.25;
        double partialSortEnd = 0.75;
        Integer[][] as_random = new Integer[ns.length][];
        Integer[][] as_sorted = new Integer[ns.length][];
        Integer[][] as_partial = new Integer[ns.length][];
        Integer[][] as_reverse = new Integer[ns.length][];
        double[][] results = new double[ns.length][4];
        for (int i = 0; i < ns.length; i++) {
            int n = ns[i];
            int[] _a = new Source(n, n).intsSupplier(10).get();
            Integer[] a = IntStream.of(_a).boxed().toArray(Integer[]::new);
            as_random[i] = a.clone();
            Arrays.sort(a, (int) (a.length * partialSortStart), (int) (a.length * partialSortEnd));
            as_partial[i] = a.clone();
            Arrays.sort(a);
            as_sorted[i] = a.clone();
            Arrays.sort(a, Collections.reverseOrder());
            as_reverse[i] = a.clone();
        }
        for (int i = 0; i < ns.length; i++) {
            int n = ns[i];
            try (InsertionSortComparator<Integer> sorter = new InsertionSortComparator<>(DESCRIPTION, Integer::compareTo, n, runs, Config_Benchmark.setupConfigFixes())) {
                // warmup
                for (int j = 0; j < 10; j++) {
                    sorter.sort(as_random[i].clone(), 0, n);
                    sorter.sort(as_sorted[i].clone(), 0, n);
                    sorter.sort(as_partial[i].clone(), 0, n);
                    sorter.sort(as_reverse[i].clone(), 0, n);
                }

                long start = System.nanoTime();
                sorter.sort(as_random[i], 0, n);
                results[i][0] = (System.nanoTime() - start) / 1e6 / runs;
                start = System.nanoTime();
                sorter.sort(as_sorted[i], 0, n);
                results[i][1] = (System.nanoTime() - start) / 1e6 / runs;
                start = System.nanoTime();
                sorter.sort(as_partial[i], 0, n);
                results[i][2] = (System.nanoTime() - start) / 1e6 / runs;
                start = System.nanoTime();
                sorter.sort(as_reverse[i], 0, n);
                results[i][3] = (System.nanoTime() - start) / 1e6 / runs;
            }
        }
        for (int i = 0; i < ns.length; i++) {
            System.out.printf("%d\t%.2f\t%.2f\t%.2f\t%.2f%n", ns[i], results[i][0], results[i][1], results[i][2], results[i][3]);
        }
    }
}