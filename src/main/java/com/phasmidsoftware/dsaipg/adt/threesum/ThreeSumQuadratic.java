/*
 * Copyright (c) 2024. Robin Hillyard
 */

package com.phasmidsoftware.dsaipg.adt.threesum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Implementation of ThreeSum which follows the approach of dividing the solution-space into
 * N sub-spaces where each sub-space corresponds to a fixed value for the middle index of the three values.
 * Each sub-space is then solved by expanding the scope of the other two indices outwards from the starting point.
 * Since each sub-space can be solved in O(N) time, the overall complexity is O(N^2).
 * <p>
 * NOTE: The array provided in the constructor MUST be ordered.
 */
public class ThreeSumQuadratic implements ThreeSum {
    /**
     * Construct a ThreeSumQuadratic on a.
     *
     * @param a a sorted array.
     */
    public ThreeSumQuadratic(int[] a) {
        this.a = a;
        length = a.length;
    }

    /**
     * Retrieves an array of unique Triples. Each Triple represents a unique combination of three integers from
     * the source array that sum to zero.
     *
     * @return an array of distinct Triples, sorted in natural order, where each Triple satisfies the condition that
     * the sum of its three integers is zero.
     */
    public Triple[] getTriples() {
        List<Triple> triples = new ArrayList<>();
        for (int i = 0; i < length; i++) triples.addAll(getTriples(i));
        Collections.sort(triples);
        return triples.stream().distinct().toArray(Triple[]::new);
    }

    /**
     * Get an array of times in milliseconds for the three steps of the algorithm:
     *
     * @return an array of times in milliseconds for the three steps of the algorithm:
     */
    public double[] getTriplesTimed() {
        List<Triple> triples = new ArrayList<>();
        double[] times = {0.0, 0.0, 0.0};
        long start = System.nanoTime();
        for (int i = 0; i < length; i++) triples.addAll(getTriples(i));
        times[0] = (System.nanoTime() - start) / 1e6;
        start = System.nanoTime();
        Collections.sort(triples);
        times[1] = (System.nanoTime() - start) / 1e6;
        start = System.nanoTime();
        triples.stream().distinct().toArray(Triple[]::new);
        times[2] = (System.nanoTime() - start) / 1e6;
        return times;
    }

    public int[] getTriplesCount() {
        List<Triple> triples = new ArrayList<>();
        for (int i = 0; i < length; i++) triples.addAll(getTriples(i));
        Collections.sort(triples);
        Triple[] distinctTriples = triples.stream().distinct().toArray(Triple[]::new);
        return new int[]{triples.size(), distinctTriples.length};
    }

    /**
     * Get a list of Triples such that the middle index is the given value j.
     *
     * @param j the index of the middle value.
     * @return a Triple such that
     */
    List<Triple> getTriples(int j) {
        List<Triple> triples = new ArrayList<>();
        int i = j - 1;
        int k = j + 1;
        while (i >= 0 && k < length) {
            int sum = a[i] + a[j] + a[k];
            if (sum == 0) {
                triples.add(new Triple(a[i], a[j], a[k]));
                i--;
                k++;
            } else if (sum < 0) {
                k++;
            } else {
                i--;
            }
        }
        return triples;
    }

    private final int[] a;
    private final int length;

    public static void timeBenchmark() {
        int[] ns = {250, 500, 1000, 2000, 4000, 8000, 16000};
        for (int n : ns) {
            double[] times = {0.0, 0.0, 0.0};
            int m = 100;
            for (int i = 0; i < m; i++) { // warm up
                int[] a = new Source(n, n).intsSupplier(10).get();
                Arrays.sort(a);
                double[] tmp = new ThreeSumQuadratic(a).getTriplesTimed();
            }
            for (int i = 0; i < m; i++) {
                int[] a = new Source(n, n).intsSupplier(10).get();
                Arrays.sort(a);
                double[] tmp = new ThreeSumQuadratic(a).getTriplesTimed();
                for (int j = 0; j < 3; j++) {
                    times[j] += tmp[j];
                }
            }
            for (int j = 0; j < 3; j++) {
                times[j] /= m;
            }
            System.out.println("For n = " + n + ": " + times[0] + ", " + times[1] + ", " + times[2]);
        }
    }

    public static void countBenchmark(){
        int[] ns = {250, 500, 1000, 2000, 4000, 8000, 16000};
        for (int n : ns) {
            int[] count = {0, 0};
            int m = 100;
            for (int i = 0; i < m; i++) {
                int[] a = new Source(n, n).intsSupplier(10).get();
                Arrays.sort(a);
                int[] tmp = new ThreeSumQuadratic(a).getTriplesCount();
                count[0] += tmp[0];
                count[1] += tmp[1];
            }
            System.out.println("For n = " + n + ", raw: " + count[0] + ", distinct: " + count[1]);
        }
    }

    public static void showTriples() {
        int n = 50;
        int[] a = new Source(n, n).intsSupplier(10).get();
        Arrays.sort(a);
        System.out.println(Arrays.toString(a));
        Triple[] triples = new ThreeSumQuadratic(a).getTriples();
        System.out.println(Arrays.toString(triples));
    }

    public static void main(String[] args) {
        showTriples();
    }
}