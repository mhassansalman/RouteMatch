package com.routematch;

import java.util.*;

public class RequestScorer {

    // ═══════════════════════════════════════════════════════════════
    // score — calculates favourability score for each shortlisted request
    //
    // FORMULA:
    // detourCost = dist(A→P) + dist(P→D) + dist(D→B) - dist(A→B)
    // score      = 1.0 / (1.0 + detourCost)
    //
    // score range: (0, 1] — higher is better
    // score = 1.0 means zero detour (passenger perfectly on route)
    // score → 0 means massive detour (passenger far off route)
    //
    // WHY NOT -detourCost:
    // detourCost = 0 gives score 0 — indistinguishable from unscored.
    // 1/(1+detourCost) always produces a meaningful positive value.
    //
    // 3 Dijkstra calls per request — justified because com.routematch.GridFilter
    // already cut candidates to a small shortlist before this runs.
    // ═══════════════════════════════════════════════════════════════
    public static void score(
            List<Request> shortlisted,
            Map<Integer, List<int[]>> adjList,
            int totalLocations,
            int source,      // A — rider start
            int dest,        // B — rider destination
            int originalTime // dist(A→B) — already computed, no extra Dijkstra
    ) {
        for (Request req : shortlisted) {
            int pickup  = req.pickupNode;
            int dropoff = req.dropOffNode;

            // 3 Dijkstra calls — A→P, P→D, D→B
            int aToPTime = RouteFinder.findShortestTime(adjList, source,  pickup,  totalLocations);
            int pToDTime = RouteFinder.findShortestTime(adjList, pickup,  dropoff, totalLocations);
            int dToBTime = RouteFinder.findShortestTime(adjList, dropoff, dest,    totalLocations);

            int detourCost = (aToPTime + pToDTime + dToBTime) - originalTime;

            // score ∈ (0,1] — stored on request for Max-Heap insertion in Chunk 4
            req.score = 1.0 / (1.0 + detourCost);        }
    }
}