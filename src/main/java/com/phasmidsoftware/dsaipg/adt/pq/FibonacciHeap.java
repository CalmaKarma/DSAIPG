package com.phasmidsoftware.dsaipg.adt.pq;

import java.util.*;

public class FibonacciHeap<T> {
    private Node<T> min;
    private int size;

    private static class Node<T> {
        T data;
        int key;
        int degree;
        Node<T> parent, child, next, prev;
        boolean marked;

        public Node(T data, int key) {
            this.data = data;
            this.key = key;
            this.next = this;
            this.prev = this;
        }
    }

    public FibonacciHeap() {
        min = null;
        size = 0;
    }

    public Node<T> insert(T data, int key) {
        Node<T> node = new Node<>(data, key);
        min = mergeLists(min, node);
        size++;
        return node;
    }

    public T findMin() {
        return min == null ? null : min.data;
    }

    public FibonacciHeap<T> union(FibonacciHeap<T> other) {
        FibonacciHeap<T> newHeap = new FibonacciHeap<>();
        newHeap.min = mergeLists(this.min, other.min);
        newHeap.size = this.size + other.size;
        this.size = 0;
        other.size = 0;
        return newHeap;
    }

    public T extractMin() {
        if (min == null) return null;

        Node<T> oldMin = min;
        if (min.child != null) {
            Node<T> child = min.child;
            do {
                child.parent = null;
                child = child.next;
            } while (child != min.child);
            mergeLists(min, min.child);
        }

        removeNode(min);
        if (min == min.next) {
            min = null;
        } else {
            min = min.next;
            consolidate();
        }

        size--;
        return oldMin.data;
    }

    public void decreaseKey(Node<T> node, int newKey) {
        if (newKey > node.key) throw new IllegalArgumentException("New key is greater than current key");
        node.key = newKey;
        Node<T> parent = node.parent;
        if (parent != null && node.key < parent.key) {
            cut(node, parent);
            cascadingCut(parent);
        }
        if (node.key < min.key) min = node;
    }

    public void delete(Node<T> node) {
        decreaseKey(node, Integer.MIN_VALUE);
        extractMin();
    }

    private void consolidate() {
        int maxDegree = (int) Math.floor(Math.log(size) / Math.log(2)) + 1;
        List<Node<T>> degrees = new ArrayList<>(Collections.nCopies(maxDegree, null));

        List<Node<T>> rootList = new ArrayList<>();
        Node<T> curr = min;
        do {
            rootList.add(curr);
            curr = curr.next;
        } while (curr != min);

        for (Node<T> node : rootList) {
            int d = node.degree;
            while (degrees.get(d) != null) {
                Node<T> other = degrees.get(d);
                if (node.key > other.key) {
                    Node<T> temp = node;
                    node = other;
                    other = temp;
                }
                link(other, node);
                degrees.set(d, null);
                d++;
            }
            degrees.set(d, node);
        }

        min = null;
        for (Node<T> node : degrees) {
            if (node != null) {
                min = mergeLists(min, node);
            }
        }
    }

    private void link(Node<T> child, Node<T> parent) {
        removeNode(child);
        child.parent = parent;
        child.marked = false;
        parent.child = mergeLists(parent.child, child);
        parent.degree++;
    }

    private void cut(Node<T> node, Node<T> parent) {
        removeNode(node);
        parent.degree--;
        min = mergeLists(min, node);
        node.parent = null;
        node.marked = false;
    }

    private void cascadingCut(Node<T> node) {
        Node<T> parent = node.parent;
        if (parent != null) {
            if (!node.marked) {
                node.marked = true;
            } else {
                cut(node, parent);
                cascadingCut(parent);
            }
        }
    }

    private static <T> Node<T> mergeLists(Node<T> a, Node<T> b) {
        if (a == null) return b;
        if (b == null) return a;
        Node<T> aNext = a.next;
        a.next = b.next;
        a.next.prev = a;
        b.next = aNext;
        b.next.prev = b;
        return a.key < b.key ? a : b;
    }

    private static <T> void removeNode(Node<T> node) {
        if (node.next == node) return;
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    public boolean isEmpty() {
        return min == null;
    }

    public int size() {
        return size;
    }
}
