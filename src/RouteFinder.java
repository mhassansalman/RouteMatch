import java.util.*;

public class RouteFinder {

    // ═══════════════════════════════════════════════════════════════
    // RouteResult — holds everything the caller needs after a query
    // path      : ordered list of node IDs from source to destination
    // legTimes  : travel time for each individual road segment
    // totalTime : total journey time (O(1) lookup, no extra loop)
    // ═══════════════════════════════════════════════════════════════
    static class RouteResult {
        List<Integer> path;
        List<Integer> legTimes;
        int totalTime;

        RouteResult(List<Integer> path, List<Integer> legTimes, int totalTime) {
            this.path = path;
            this.legTimes = legTimes;
            this.totalTime = totalTime;
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
            int totalLocations
    ) {
        // 1. Setup Tracking Arrays
        // shortestTimes[i]    = best known travel time to reach node i
        // previousLocation[i] = which node we came from to reach i (breadcrumb trail)
        int[] shortestTimes = new int[totalLocations];
        int[] previousLocation = new int[totalLocations];

        Arrays.fill(shortestTimes, Integer.MAX_VALUE); // all unreachable at start
        Arrays.fill(previousLocation, -1);             // no path known yet

        // 2. Min-Heap (Priority Queue)
        // Always expands the cheapest unvisited node first
        // Each entry: [nodeID, cumulativeTravelTime]
        PriorityQueue<int[]> minHeap = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));

        shortestTimes[startLocation] = 0;
        minHeap.offer(new int[]{startLocation, 0});

        // 3. Core Dijkstra Loop
        while (!minHeap.isEmpty()) {
            int[] current = minHeap.poll();
            int currentLocation = current[0];
            int timeToCurrent = current[1];

            // Early exit: once destination is popped, shortest path is finalized
            if (currentLocation == endLocation) break;

            // Skip stale entries — a faster path to this node was already found
            if (timeToCurrent > shortestTimes[currentLocation]) continue;

            // Explore all roads going out from current node
            if (graph.containsKey(currentLocation)) {
                for (int[] edge : graph.get(currentLocation)) {
                    int neighborLocation = edge[0];
                    int roadTravelTime  = edge[1];
                    int potentialTime   = shortestTimes[currentLocation] + roadTravelTime;

                    // Relaxation: found a faster way to reach this neighbor?
                    if (potentialTime < shortestTimes[neighborLocation]) {
                        shortestTimes[neighborLocation] = potentialTime;
                        previousLocation[neighborLocation] = currentLocation;
                        minHeap.offer(new int[]{neighborLocation, potentialTime});
                    }
                }
            }
        }

        // 4. Reconstruct path and compute per-leg times
        List<Integer> path     = reconstructPath(startLocation, endLocation, previousLocation);
        List<Integer> legTimes = computeLegTimes(path, shortestTimes);
        return new RouteResult(path, legTimes, shortestTimes[endLocation]);
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

    // Traces backward from destination using breadcrumb trail, then reverses
    // Result: [start, ..., end]
    private static List<Integer> reconstructPath(int start, int end, int[] previousLocation) {
        List<Integer> path = new ArrayList<>();
        if (previousLocation[end] == -1 && start != end) return path; // unreachable
        for (int at = end; at != -1; at = previousLocation[at])
            path.add(at);
        Collections.reverse(path);
        return path;
    }
}