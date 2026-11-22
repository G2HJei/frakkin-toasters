package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Picks of elements for the candidate answers: subsets of a hand, orders of a deck, one of each group. Equal elements
 * are kept apart by position, because a hand holds equal cards.
 */
final class Combinations {

    private Combinations() {
    }

    /**
     * @return every pick of {@code size} elements in their order, or {@code limit} random ones when there are more
     */
    static <T> List<List<T>> subsets(List<T> elements, int size, int limit, Random random) {
        if (size < 0 || size > elements.size()) {
            return List.of();
        }
        if (binomial(elements.size(), size) <= limit) {
            val result = new ArrayList<List<T>>();
            collect(elements, size, 0, new ArrayList<>(), result);
            Collections.shuffle(result, random);
            return result;
        }
        val result = new ArrayList<List<T>>();
        for (int i = 0; i < limit; i++) {
            result.add(randomSubset(elements, size, random));
        }
        return result;
    }

    /**
     * @return picks of every size from {@code minSize} to {@code maxSize}, about as many of each size, in random order
     */
    static <T> List<List<T>> subsets(List<T> elements, int minSize, int maxSize, int limit, Random random) {
        val sizes = Math.max(1, maxSize - minSize + 1);
        val result = new ArrayList<List<T>>();
        for (int size = minSize; size <= Math.min(maxSize, elements.size()); size++) {
            result.addAll(subsets(elements, size, Math.max(1, limit / sizes), random));
        }
        Collections.shuffle(result, random);
        return result;
    }

    /**
     * @return the orders of {@code size} elements, or {@code limit} random ones when there are more
     */
    static <T> List<List<T>> arrangements(List<T> elements, int size, int limit, Random random) {
        val result = new ArrayList<List<T>>();
        for (val subset : subsets(elements, size, limit, random)) {
            result.addAll(permutations(subset));
            if (result.size() >= limit) {
                break;
            }
        }
        Collections.shuffle(result, random);
        return result.size() > limit ? List.copyOf(result.subList(0, limit)) : result;
    }

    static <T> List<List<T>> permutations(List<T> elements) {
        if (elements.size() <= 1) {
            return List.of(List.copyOf(elements));
        }
        val result = new ArrayList<List<T>>();
        for (int i = 0; i < elements.size(); i++) {
            val rest = new ArrayList<>(elements);
            val first = rest.remove(i);
            for (val tail : permutations(rest)) {
                val permutation = new ArrayList<T>();
                permutation.add(first);
                permutation.addAll(tail);
                result.add(permutation);
            }
        }
        return result;
    }

    /**
     * @return one element of each group, every way
     */
    static <T> List<List<T>> oneOfEach(List<List<T>> groups) {
        List<List<T>> result = List.of(List.of());
        for (val group : groups) {
            val next = new ArrayList<List<T>>();
            for (val prefix : result) {
                for (val element : group) {
                    val pick = new ArrayList<>(prefix);
                    pick.add(element);
                    next.add(pick);
                }
            }
            result = next;
        }
        return result;
    }

    /**
     * @return every way to put {@code size} items into {@code bins} bins, as the count in each bin
     */
    static List<int[]> multisets(int bins, int size) {
        val result = new ArrayList<int[]>();
        fill(new int[bins], 0, size, result);
        return result;
    }

    static long binomial(int n, int k) {
        long result = 1;
        for (int i = 1; i <= k; i++) {
            result = result * (n - k + i) / i;
            if (result > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
        }
        return result;
    }

    private static <T> void collect(List<T> elements, int size, int from, List<T> prefix, List<List<T>> result) {
        if (prefix.size() == size) {
            result.add(List.copyOf(prefix));
            return;
        }
        for (int i = from; i <= elements.size() - (size - prefix.size()); i++) {
            prefix.add(elements.get(i));
            collect(elements, size, i + 1, prefix, result);
            prefix.removeLast();
        }
    }

    private static <T> List<T> randomSubset(List<T> elements, int size, Random random) {
        val indices = new ArrayList<Integer>();
        for (int i = 0; i < elements.size(); i++) {
            indices.add(i);
        }
        Collections.shuffle(indices, random);
        // The elements keep their order in the hand, so equal picks are equal lists.
        return indices.subList(0, size).stream().sorted().map(elements::get).toList();
    }

    private static void fill(int[] counts, int bin, int left, List<int[]> result) {
        if (bin == counts.length - 1) {
            counts[bin] = left;
            result.add(counts.clone());
            return;
        }
        for (int count = left; count >= 0; count--) {
            counts[bin] = count;
            fill(counts, bin + 1, left - count, result);
        }
    }
}
