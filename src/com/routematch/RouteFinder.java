package com.routematch;

import java.util.*;

public class RouteFinder {

    // ═══════════════════════════════════════════════════════════════
    // RouteResult — holds everything the caller needs after a query
    // path       : ordered list of node IDs from source to destination
    // legTimes   : travel time for each individual road segment
    // totalTime  : total journey time (O(1) lookup, no extra loop)
    // routeCells : grid cells the route passes through — collected
    //              during path reconstruction (loop fusion) to avoid
    //              a second O(n) pass in com.routematch.GridFilter. At this scale
    //              (n ≈ 6) the gain is negligible — done here for
    //              architectural completeness. At 10,000+ nodes this
    //              matters. An alternative HashSet approach was kept
    //              in the chunk2-separate branch for comparison.
    // ═══════════════════════════════════════════════════════════════
    static class RouteResult {
        List<Integer> path;
        List<Integer> legTimes;
        int totalTime;
        Set<String> routeCells; // "x,y" keys — O(1) lookup in com.routematch.GridFilter

        RouteResult(List<Integer> path, List<Integer> legTimes,
                    int totalTime, Set<String> routeCells) {
            this.path       = path;
            this.legTimes   = legTimes;
            this.totalTime  = totalTime;
            this.routeCells = routeCells;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // APPROACH 1: ON-DEMAND DIJKSTRA (active)
    // Runs Dijkstra fresh every time a rider queries source → destination
    // Good for: large/dynamic networks (real-world apps like Google Maps)
    // Google Maps uses A* + contraction hierarchies, not plain Dijkstra
    // ═══════════════════════════════════════════════════════════════
    public static RouteResult findShortestRoute(
            Map<Integer, List<int[]>> graph,
            int startLocation,
            int endLocation,
            int totalLocations,
            Node[] nodes            // needed for grid cell fusion
    ) {
        // 1. Setup Tracking Arrays
        // shortestTimes[i]    = best known travel time to reach node i
        // previousLocation[i] = which node we came from to reach i (breadcrumb trail)
        int[] shortestTimes     = new int[totalLocations];
        int[] previousLocation  = new int[totalLocations];

        Arrays.fill(shortestTimes, Integer.MAX_VALUE); // all unreachable at start
        Arrays.fill(previousLocation, -1);             // no path known yet

        // 2. Min-Heap (Priority Queue)
        // Always expands the cheapest unvisited node first
        // Each entry: [nodeID, cumulativeTravelTime]
        PriorityQueue<int[]> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));

        shortestTimes[startLocation] = 0;
        minHeap.offer(new int[]{startLocation, 0});

        // 3. Core Dijkstra Loop
        while (!minHeap.isEmpty()) {
            int[] current        = minHeap.poll();
            int currentLocation  = current[0];
            int timeToCurrent    = current[1];

            // Early exit: once destination is popped, shortest path is finalized
            if (currentLocation == endLocation) break;

            // Skip stale entries — a faster path to this node was already found
            if (timeToCurrent > shortestTimes[currentLocation]) continue;

            // Explore all roads going out from current node
            if (graph.containsKey(currentLocation)) {
                for (int[] edge : graph.get(currentLocation)) {
                    int neighborLocation = edge[0];
                    int roadTravelTime   = edge[1];
                    int potentialTime    = shortestTimes[currentLocation] + roadTravelTime;

                    // Relaxation: found a faster way to reach this neighbor?
                    if (potentialTime < shortestTimes[neighborLocation]) {
                        shortestTimes[neighborLocation] = potentialTime;
                        previousLocation[neighborLocation] = currentLocation;
                        minHeap.offer(new int[]{neighborLocation, potentialTime});
                    }
                }
            }
        }

        // 4. Reconstruct path + collect grid cells in one pass (loop fusion)
        List<Integer> path          = new ArrayList<>();
        Set<String>   routeCells    = new HashSet<>();

        reconstructPathAndCells(startLocation, endLocation,
                previousLocation, nodes, path, routeCells);

        List<Integer> legTimes = computeLegTimes(path, shortestTimes);
        return new RouteResult(path, legTimes, shortestTimes[endLocation], routeCells);
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    // Per-leg time = cumulative time at node[i] minus cumulative time at node[i-1]
    // No edge looping needed — shortestTimes already has everything
    private static List<Integer> computeLegTimes(List<Integer> path, int[] shortestTimes) {
        List<Integer> legTimes = new ArrayList<>();
        for (int i = 1; i < path.size(); i++)
            legTimes.add(shortestTimes[path.get(i)] - shortestTimes[path.get(i - 1)]);
        return legTimes;
    }

    // Traces backward from destination using breadcrumb trail, then reverses.
    // Loop fusion: grid cell ("x,y") for each node collected in the same pass —
    // avoids a second O(n) iteration in com.routematch.GridFilter.
    // Result path: [start, ..., end]
    private static void reconstructPathAndCells(
            int start, int end,
            int[] previousLocation,
            Node[] nodes,
            List<Integer> path,       // populated here
            Set<String> routeCells    // populated here — fusion output
    ) {
        if (previousLocation[end] == -1 && start != end) return; // unreachable

        for (int at = end; at != -1; at = previousLocation[at]) {
            path.add(at);
            // Loop fusion: collect grid cell while tracing path
            // cell key "x,y" uniquely identifies each grid cell for O(1) HashSet lookup
            routeCells.add(nodes[at].x + "," + nodes[at].y);
        }
        Collections.reverse(path);
        // routeCells is a Set — order doesn't matter, no reverse needed
    }



    // Distance-only Dijkstra — used by Chunk 3 scoring (3 calls per request)
    // Duplicated intentionally from findShortestRoute — shared core (runDijkstra)
    // was considered but rejected. Sharing loses early exit, which matters at
    // scale (large graphs, 60+ scoring calls). DRY acknowledged — performance wins.
    public static int findShortestTime(
            Map<Integer, List<int[]>> graph,
            int startLocation,
            int endLocation,
            int totalLocations
    ) {
        int[] shortestTimes = new int[totalLocations];
        Arrays.fill(shortestTimes, Integer.MAX_VALUE);
        shortestTimes[startLocation] = 0;

        PriorityQueue<int[]> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        minHeap.offer(new int[]{startLocation, 0});

        while (!minHeap.isEmpty()) {
            int[] current       = minHeap.poll();
            int currentLocation = current[0];
            int timeToCurrent   = current[1];

            // Early exit — destination finalized, stop expanding
            if (currentLocation == endLocation) break;

            if (timeToCurrent > shortestTimes[currentLocation]) continue;

            if (graph.containsKey(currentLocation)) {
                for (int[] edge : graph.get(currentLocation)) {
                    int neighbor      = edge[0];
                    int travelTime    = edge[1];
                    int potentialTime = shortestTimes[currentLocation] + travelTime;

                    if (potentialTime < shortestTimes[neighbor]) {
                        shortestTimes[neighbor] = potentialTime;
                        minHeap.offer(new int[]{neighbor, potentialTime});
                    }
                }
            }
        }

        return shortestTimes[endLocation];
    }
}