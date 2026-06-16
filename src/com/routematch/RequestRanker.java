package com.routematch;

import java.util.*;

public class RequestRanker {

    // ═══════════════════════════════════════════════════════════════
    // rank — inserts scored requests into a Max-Heap, extracts in order
    //
    // WHY MAX-HEAP:
    // We want highest score (least detour) at the top — max-heap
    // gives O(1) access to best match, O(log n) insert and extract.
    //
    // Java's PriorityQueue is min-heap by default.
    // Reversed comparator flips it to max-heap — no custom structure needed.
    //
    // COMPLEXITY:
    // Insert all n requests: O(n log n)
    // Extract all n requests: O(n log n)
    // Overall: O(n log n) — dominated by heap operations
    // ═══════════════════════════════════════════════════════════════
    public static List<Request> rank(List<Request> shortlisted) {

        // Max-Heap — highest score extracted first
        // Comparator reversed from default min-heap behaviour
        PriorityQueue<Request> maxHeap = new PriorityQueue<>(
                (a, b) -> Double.compare(b.score, a.score)
        );

        // Insert all scored requests — O(log n) per insert
        for (Request req : shortlisted)
            maxHeap.offer(req);

        // Extract in ranked order — O(log n) per extract
        // Result: best match first, worst last
        List<Request> ranked = new ArrayList<>();
        while (!maxHeap.isEmpty())
            ranked.add(maxHeap.poll());

        return ranked;
    }
}