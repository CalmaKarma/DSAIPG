/*
 * Copyright (c) 2024. Robin Hillyard
 */

package com.phasmidsoftware.dsaipg.sort.par;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;


/**
 * This code has been fleshed out by Ziyao Qiao. Thanks very much.
 * CONSIDER tidy it up a bit.
 */
public class Main {

    private static final int ARRAY_SIZE = 2_000_000;
    private static final int NUM_ITERATIONS = 10;
    private static final int INITIAL_CUTOFF = 1_000;
    private static final int MAX_CUTOFF = ARRAY_SIZE / 2;
    private static final int WARMUP_ITERATIONS = 10;
    private static final int INITIAL_WORKERS = 2;
    private static final int MAX_WORKERS = 64;
    private static final String OUTPUT_FILE = "./src/result.csv";

    public static void main(String[] args) {
        processArgs(args);
        System.out.println("Degree of parallelism: " + ForkJoinPool.getCommonPoolParallelism());

        Random random = new Random();
        int[] array = new int[ARRAY_SIZE];
        ArrayList<String> timeList = new ArrayList<>();

        System.out.println("Starting warmup phase...");
        ParSort.executor = new ForkJoinPool(15);
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            fillArray(array, random);
            ParSort.sort(array, 0, array.length);
        }
        System.out.println("Warmup phase completed.");

        for (int workers = INITIAL_WORKERS; workers <= MAX_WORKERS; workers *= 2) {
            ForkJoinPool pool = new ForkJoinPool(workers);
            ParSort.executor = pool;

            for (int cutoff = INITIAL_CUTOFF; cutoff <= MAX_CUTOFF; cutoff *= 2) {
                ParSort.cutoff = cutoff;

                double totalTime = 0;
                for (int t = 0; t < NUM_ITERATIONS; t++) {
                    fillArray(array, random);

                    long startTime = System.nanoTime();
                    pool.invoke(new SortTask(array, 0, array.length));
                    long endTime = System.nanoTime();

                    totalTime += (endTime - startTime);
                }

                double avgTime = totalTime / NUM_ITERATIONS / 1e6;
                timeList.add(workers + "," + cutoff + "," + (double) cutoff / ARRAY_SIZE + "," + avgTime);
                System.out.println("Workers: " + workers + "\tCutoff: " + cutoff + "\tRatio: " + (double) cutoff / ARRAY_SIZE + "\tAvg Time: " + avgTime + "ms");
            }
            pool.shutdown();
        }

        benchmarkBaseline(array, random, timeList);
        writeResultsToFile(timeList);
    }

    private static void fillArray(int[] array, Random random) {
        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt(10_000_000);
        }
    }

    private static void benchmarkBaseline(int[] array, Random random, ArrayList<String> timeList) {
        double totalTime = 0;
        for (int t = 0; t < NUM_ITERATIONS; t++) {
            fillArray(array, random);
            long startTime = System.nanoTime();
            Arrays.sort(array);
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }
        double avgTime = totalTime / NUM_ITERATIONS / 1e6;
        timeList.add("Baseline,Array.sort,1.0," + avgTime);
        System.out.println("Baseline: Array.sort \t Avg Time: " + avgTime + "ms");
    }

    private static void writeResultsToFile(ArrayList<String> timeList) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUTPUT_FILE)))) {
            bw.write("Workers,Cutoff,Ratio,Avg Time (ms)\n");
            for (String entry : timeList) {
                bw.write(entry + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void processArgs(String[] args) {
        String[] xs = args;
        while (xs.length > 0)
            if (xs[0].startsWith("-")) xs = processArg(xs);
    }

    private static String[] processArg(String[] xs) {
        String[] result = new String[0];
        System.arraycopy(xs, 2, result, 0, xs.length - 2);
        processCommand(xs[0], xs[1]);
        return result;
    }

    private static void processCommand(String x, String y) {
        if (x.equalsIgnoreCase("N")) setConfig(x, Integer.parseInt(y));
        else
            // TODO sort this out
            if (x.equalsIgnoreCase("P")) //noinspection ResultOfMethodCallIgnored
                ForkJoinPool.getCommonPoolParallelism();
    }

    private static void setConfig(String x, int i) {
        configuration.put(x, i);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final Map<String, Integer> configuration = new HashMap<>();

    static class SortTask extends RecursiveAction {
        private final int[] array;
        private final int from;
        private final int to;

        public SortTask(int[] array, int from, int to) {
            this.array = array;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void compute() {
            ParSort.sort(array, from, to);
        }
    }
}