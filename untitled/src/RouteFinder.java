import java.util.*;

public class RouteFinder {

    // ═══════════════════════════════════════════════════════════════
    // PRECOMPUTATION STORAGE
    // Uncomment below if using precomputed approach
    // ═══════════════════════════════════════════════════════════════
    // static int[][] allShortestTimes;
    // static int[][] allPreviousLocations;

    // Inner class to hold result — used by BOTH approaches
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
    // APPROACH 1: ON-DEMAND (currently active)
    // Runs Dijkstra fresh every time rider enters source/destination
    // Real world: Google Maps does this with A* + optimizations
    // Switch to precompute: comment this method, uncomment APPROACH 2
    // ═══════════════════════════════════════════════════════════════
    public static RouteResult findShortestRoute(
            Map<Integer, List<int[]>> graph,
            int startLocation,
            int endLocation,
            int totalLocations
    ) {
        int[] shortestTimes = new int[totalLocations];
        int[] previousLocation = new int[totalLocations];

        Arrays.fill(shortestTimes, Integer.MAX_VALUE);
        Arrays.fill(previousLocation, -1);

        PriorityQueue<int[]> minHeap = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));

        shortestTimes[startLocation] = 0;
        minHeap.offer(new int[]{startLocation, 0});

        while (!minHeap.isEmpty()) {
            int[] current = minHeap.poll();
            int currentLocation = current[0];
            int timeToCurrent = current[1];

            if (currentLocation == endLocation) break;
            if (timeToCurrent > shortestTimes[currentLocation]) continue;

            if (graph.containsKey(currentLocation)) {
                for (int[] edge : graph.get(currentLocation)) {
                    int neighborLocation = edge[0];
                    int roadTravelTime = edge[1];
                    int potentialTime = shortestTimes[currentLocation] + roadTravelTime;

                    if (potentialTime < shortestTimes[neighborLocation]) {
                        shortestTimes[neighborLocation] = potentialTime;
                        previousLocation[neighborLocation] = currentLocation;
                        minHeap.offer(new int[]{neighborLocation, potentialTime});
                    }
                }
            }
        }

        List<Integer> path = reconstructPath(startLocation, endLocation, previousLocation);
        List<Integer> legTimes = computeLegTimes(path, shortestTimes);
        return new RouteResult(path, legTimes, shortestTimes[endLocation]);
    }

    // ═══════════════════════════════════════════════════════════════
    // APPROACH 2: PRECOMPUTED (currently inactive)
    // Runs Dijkstra once from every node at startup — O(1) per query
    // Feasible here: small fixed network (10 nodes)
    // NOT feasible for real cities — millions of nodes = too much memory
    // Switch to this: uncomment both methods, comment out APPROACH 1
    // ═══════════════════════════════════════════════════════════════

    // public static void precompute(Map<Integer, List<int[]>> graph, int totalLocations) {
    //     allShortestTimes = new int[totalLocations][totalLocations];
    //     allPreviousLocations = new int[totalLocations][totalLocations];
    //
    //     for (int source = 0; source < totalLocations; source++) {
    //         int[] times = new int[totalLocations];
    //         int[] previous = new int[totalLocations];
    //         Arrays.fill(times, Integer.MAX_VALUE);
    //         Arrays.fill(previous, -1);
    //         times[source] = 0;
    //
    //         PriorityQueue<int[]> minHeap = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
    //         minHeap.offer(new int[]{source, 0});
    //
    //         while (!minHeap.isEmpty()) {
    //             int[] current = minHeap.poll();
    //             int currentLocation = current[0];
    //             int timeToCurrent = current[1];
    //             if (timeToCurrent > times[currentLocation]) continue;
    //
    //             if (graph.containsKey(currentLocation)) {
    //                 for (int[] edge : graph.get(currentLocation)) {
    //                     int neighbor = edge[0];
    //                     int roadTime = edge[1];
    //                     int potentialTime = times[currentLocation] + roadTime;
    //                     if (potentialTime < times[neighbor]) {
    //                         times[neighbor] = potentialTime;
    //                         previous[neighbor] = currentLocation;
    //                         minHeap.offer(new int[]{neighbor, potentialTime});
    //                     }
    //                 }
    //             }
    //         }
    //         allShortestTimes[source] = times;
    //         allPreviousLocations[source] = previous;
    //     }
    // }

    // public static RouteResult findShortestRoute(int startLocation, int endLocation) {
    //     List<Integer> path = reconstructPath(startLocation, endLocation, allPreviousLocations[startLocation]);
    //     List<Integer> legTimes = computeLegTimes(path, allShortestTimes[startLocation]);
    //     return new RouteResult(path, legTimes, allShortestTimes[startLocation][endLocation]);
    // }

    // ═══════════════════════════════════════════════════════════════
    // SHARED HELPERS — used by both approaches
    // ═══════════════════════════════════════════════════════════════

    // Per-leg time = cumulative time at current node minus cumulative time at previous node
    private static List<Integer> computeLegTimes(List<Integer> path, int[] shortestTimes) {
        List<Integer> legTimes = new ArrayList<>();
        for (int i = 1; i < path.size(); i++)
            legTimes.add(shortestTimes[path.get(i)] - shortestTimes[path.get(i - 1)]);
        return legTimes;
    }

    // Traces backward from destination using breadcrumb trail, then reverses
    private static List<Integer> reconstructPath(int start, int end, int[] previousLocation) {
        List<Integer> path = new ArrayList<>();
        if (previousLocation[end] == -1 && start != end) return path;
        for (int at = end; at != -1; at = previousLocation[at])
            path.add(at);
        Collections.reverse(path);
        return path;
    }
}