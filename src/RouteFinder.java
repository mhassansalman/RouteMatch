import java.util.*;

public class RouteFinder {

    // Inner class to hold both path and timing results together
    static class RouteResult {
        List<Integer> path;
        int[] shortestTimes;

        RouteResult(List<Integer> path, int[] shortestTimes) {
            this.path = path;
            this.shortestTimes = shortestTimes;
        }
    }

    /**
     * Finds the shortest path between two locations using Dijkstra's Algorithm.
     *
     * @param graph          Lahore's road network: each node maps to a list of [neighbor, travelTime]
     * @param startLocation  Starting node ID
     * @param endLocation    Destination node ID
     * @param totalLocations Total number of nodes in the graph
     * @return RouteResult containing the path and shortest times to all nodes
     */
    public static RouteResult findShortestRoute(
            Map<Integer, List<int[]>> graph,
            int startLocation,
            int endLocation,
            int totalLocations
    ) {
        // 1. Setup Tracking Arrays
        // shortestTimes[i] = best known travel time to reach location i
        // previousLocation[i] = which location we came from to reach i (breadcrumb trail)
        int[] shortestTimes = new int[totalLocations];
        int[] previousLocation = new int[totalLocations];

        Arrays.fill(shortestTimes, Integer.MAX_VALUE); // assume all unreachable at start
        Arrays.fill(previousLocation, -1);             // no path known yet

        // 2. Setup Min-Heap (Priority Queue)
        // Always processes the closest unvisited location first
        // Each entry: [locationID, travelTimeSoFar]
        PriorityQueue<int[]> minHeap = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));

        // Start location costs 0 to reach
        shortestTimes[startLocation] = 0;
        minHeap.offer(new int[]{startLocation, 0});

        // 3. Core Dijkstra Loop
        while (!minHeap.isEmpty()) {
            int[] current = minHeap.poll();
            int currentLocation = current[0];
            int timeToCurrent = current[1];

            // Early exit: destination reached, no need to explore further
            if (currentLocation == endLocation) break;

            // Skip stale entries — a better path to this location was already found
            if (timeToCurrent > shortestTimes[currentLocation]) continue;

            // Check all roads going out from current location
            if (graph.containsKey(currentLocation)) {
                for (int[] edge : graph.get(currentLocation)) {
                    int neighborLocation = edge[0];
                    int roadTravelTime = edge[1];
                    int potentialTime = shortestTimes[currentLocation] + roadTravelTime;

                    // Relaxation Step: found a faster way to reach this neighbor?
                    if (potentialTime < shortestTimes[neighborLocation]) {
                        shortestTimes[neighborLocation] = potentialTime;
                        previousLocation[neighborLocation] = currentLocation;
                        minHeap.offer(new int[]{neighborLocation, potentialTime});
                    }
                }
            }
        }

        // 4. Reconstruct path and return alongside shortestTimes
        List<Integer> path = reconstructPath(startLocation, endLocation, previousLocation);
        return new RouteResult(path, shortestTimes);
    }

    /**
     * Traces backward from destination to source using previousLocation breadcrumbs,
     * then reverses so path reads Start → ... → Destination.
     */
    private static List<Integer> reconstructPath(int start, int end, int[] previousLocation) {
        List<Integer> path = new ArrayList<>();

        // Destination unreachable — no path exists
        if (previousLocation[end] == -1 && start != end) return path;

        // Walk backward: end → ... → start
        for (int at = end; at != -1; at = previousLocation[at])
            path.add(at);

        // Reverse to get correct order: start → ... → end
        Collections.reverse(path);
        return path;
    }
}