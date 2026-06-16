package com.routematch;

import java.util.*;

public class Main {
    public static void main(String[] args) {

        // com.routematch.Graph size read from nodes.csv — dynamic, no hardcoded size
        Graph graph = new Graph();

        // Load road network from CSV — simulates a real city database.
        // In production: nodes and edges load from MySQL locations/roads tables.
        loadNodes("nodes.csv", graph);
        loadEdges("edges.csv", graph);

        graph.printGraph();

        // Load passenger requests from CSV — simulates a live request feed.
        // In production: MySQL query on ride_requests table.
        // CSV chosen here to keep DSA logic isolated from DB dependencies.
        List<Request> allRequests = loadRequests("requests.csv");

        // Pre-bucket requests by pickup cell into a spatial HashMap
        // Room "x,y" → all requests whose pickup falls in that cell
        // O(R) one-time setup — avoids O(R) scan on every future query
        // Alternative: O(R) scan per query kept in no-prefilter branch
        Map<String, List<Request>> requestGrid = new HashMap<>();
        for (Request req : allRequests) {
            Node pickup = graph.nodes[req.pickupNode];
            String cell = pickup.x + "," + pickup.y;
            requestGrid.computeIfAbsent(cell, k -> new ArrayList<>()).add(req);
        }

        // Get rider's source and destination
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter source ID: ");
        int source = scanner.nextInt();
        System.out.print("Enter destination ID: ");
        int dest = scanner.nextInt();

        // Chunk 1: find shortest route
        // nodes[] passed for grid cell fusion inside com.routematch.RouteFinder
        RouteFinder.RouteResult result = RouteFinder.findShortestRoute(
                graph.adjList, source, dest, graph.numNodes, graph.nodes);
        printPath(result, graph);

        // Chunk 2: spatially filter requests near rider's route
        // radius 1 = 3×3 block — expand if too few results found
        int radius = 1;
        List<Request> shortlisted = GridFilter.filter(
                requestGrid, result.routeCells, graph.nodes, radius);
        printShortlisted(shortlisted, radius, graph);

        // Chunk 3: score each shortlisted request by detour cost
        // formula: score = 1 / (1 + detourCost) — range (0,1]
        RequestScorer.score(
                shortlisted, graph.adjList, graph.numNodes,
                source, dest, result.totalTime);

        // Chunk 4: rank by score using Max-Heap — best match first
        List<Request> ranked = RequestRanker.rank(shortlisted);
        printRanked(ranked, graph);
    }

    // ── Data loaders ─────────────────────────────────────────────

    static void loadNodes(String filename, Graph graph) {
        try (Scanner file = new Scanner(new java.io.File(filename))) {
            // First line = node count — used to allocate com.routematch.Node[] upfront
            // Keeps com.routematch.Node[] performance (O(1) index) while staying dynamic
            int count = Integer.parseInt(file.nextLine().trim());
            graph.initNodes(count);
            file.nextLine(); // skip header
            while (file.hasNextLine()) {
                String[] p = file.nextLine().split(",");
                graph.addNode(
                        Integer.parseInt(p[0].trim()),
                        p[1].trim(),
                        Integer.parseInt(p[2].trim()),
                        Integer.parseInt(p[3].trim())
                );
            }
        } catch (java.io.FileNotFoundException e) {
            System.out.println("nodes.csv not found.");
        }
    }

    static void loadEdges(String filename, Graph graph) {
        try (Scanner file = new Scanner(new java.io.File(filename))) {
            file.nextLine(); // skip header
            while (file.hasNextLine()) {
                String[] p = file.nextLine().split(",");
                graph.addEdge(
                        Integer.parseInt(p[0].trim()),
                        Integer.parseInt(p[1].trim()),
                        Integer.parseInt(p[2].trim())
                );
            }
        } catch (java.io.FileNotFoundException e) {
            System.out.println("edges.csv not found.");
        }
    }

    static List<Request> loadRequests(String filename) {
        List<Request> requests = new ArrayList<>();
        try (Scanner file = new Scanner(new java.io.File(filename))) {
            file.nextLine(); // skip header
            while (file.hasNextLine()) {
                String[] p = file.nextLine().split(",");
                requests.add(new Request(
                        Integer.parseInt(p[0].trim()),
                        p[1].trim(),
                        Integer.parseInt(p[2].trim()),
                        Integer.parseInt(p[3].trim())
                ));
            }
        } catch (java.io.FileNotFoundException e) {
            System.out.println("requests.csv not found — no requests loaded.");
        }
        return requests;
    }

    // ── Output helpers ───────────────────────────────────────────

    static void printPath(RouteFinder.RouteResult result, Graph graph) {
        if (result.path.isEmpty()) { System.out.println("No path found."); return; }
        System.out.print("\nRider's route: ");
        for (int i = 0; i < result.path.size(); i++) {
            System.out.print(graph.nodes[result.path.get(i)].name);
            if (i < result.path.size() - 1)
                System.out.print(" →(" + result.legTimes.get(i) + "min)→ ");
        }
        System.out.println(" | Total: " + result.totalTime + " min");
    }

    static void printShortlisted(List<Request> shortlisted, int radius, Graph graph) {
        System.out.println("\n=== Nearby Requests (radius " + radius + ") ===");
        if (shortlisted.isEmpty()) {
            System.out.println("None found. Try increasing radius.");
            return;
        }
        for (Request r : shortlisted)
            System.out.println(r.passengerName
                    + " | Pickup: "  + graph.nodes[r.pickupNode].name
                    + " → Dropoff: " + graph.nodes[r.dropOffNode].name);
    }

    static void printRanked(List<Request> ranked, Graph graph) {
        System.out.println("\n=== Ranked Passenger Requests ===");
        int rank = 1;
        for (Request r : ranked)
            System.out.printf("#%d %s | Pickup: %s → Dropoff: %s | score: %.4f%n",
                    rank++,
                    r.passengerName,
                    graph.nodes[r.pickupNode].name,
                    graph.nodes[r.dropOffNode].name,
                    r.score);
    }
}


