package com.phasmidsoftware.dsaipg.adt.pq;

import java.util.Comparator;
import java.util.function.BiPredicate;

public class FouraryHeap<K> extends PriorityQueue<K> {

    public FouraryHeap(boolean max, Object[] binHeap, int first, int last, Comparator<K> comparator, boolean floyd) {
        super(max, binHeap, first, last, comparator, floyd);
    }

    public FouraryHeap(int n, int first, boolean max, Comparator<K> comparator, boolean floyd) {

        // NOTE that we reserve the first element of the binary heap, so the length must be n+1, not n
        super(max, new Object[n + first], first, 0, comparator, floyd);
    }

    public FouraryHeap(int n, boolean max, Comparator<K> comparator, boolean floyd) {

        // NOTE that we reserve the first element of the binary heap, so the length must be n+1, not n
        super(n, 1, max, comparator, floyd);
    }

    public FouraryHeap(int n, boolean max, Comparator<K> comparator) {

        // NOTE that we reserve the first element of the binary heap, so the length must be n+1, not n
        super(n, 1, max, comparator, false);
    }

    public FouraryHeap(int n, Comparator<K> comparator) {
        super(n, 1, true, comparator, true);
    }

    @Override
    int doHeapify(int k, BiPredicate<Integer, Integer> p) {
        int i = k;
        while (firstChild(i) <= last + first - 1) {
            int j = firstChild(i);
            for (int c = 1; c < 4; c++) {
                if (j < last + first - 1 && unordered(j, j + 1)) j++;
            }
            if (p.test(i, j)) break;
            swap(i, j);
            i = j;
        }
        return i;
    }

    @Override
    int parent(int k) {
        return (k - 1 - first) / 4 + first;
    }

    @Override
    int firstChild(int k) {
        return (k - first) * 4 + first + 1;
    }
}
