import java.util.*;

import java.util.*;

public class Main {
    public static void main(String[] args) {

        // Build Lahore's road network
        Graph graph = new Graph(10);

        graph.addNode(0, "Gulberg",        3, 4);
        graph.addNode(1, "DHA",            1, 2);
        graph.addNode(2, "Johar Town",     2, 4);
        graph.addNode(3, "Model Town",     2, 5);
        graph.addNode(4, "Bahria Town",    0, 3);
        graph.addNode(5, "Airport",        5, 1);
        graph.addNode(6, "Anarkali",       4, 6);
        graph.addNode(7, "Shadman",        3, 5);
        graph.addNode(8, "Cavalry Ground", 4, 3);
        graph.addNode(9, "Cantt",          4, 2);

        graph.addEdge(0, 1, 15);
        graph.addEdge(0, 2, 10);
        graph.addEdge(0, 7, 5);
        graph.addEdge(1, 9, 8);
        graph.addEdge(1, 4, 20);
        graph.addEdge(2, 3, 7);
        graph.addEdge(2, 7, 8);
        graph.addEdge(3, 7, 6);
        graph.addEdge(3, 6, 10);
        graph.addEdge(5, 9, 12);
        graph.addEdge(5, 8, 15);
        graph.addEdge(6, 7, 8);
        graph.addEdge(7, 8, 10);
        graph.addEdge(8, 9, 7);

        graph.printGraph();

        // Hardcoded passenger requests — in production these load from MySQL
        // (ride_requests table). Kept hardcoded here to isolate DSA logic.
        List<Request> allRequests = new ArrayList<>();
        allRequests.add(new Request(0, "Ali",    2, 5)); // Johar Town → Airport
        allRequests.add(new Request(1, "Sara",   7, 5)); // Shadman → Airport
        allRequests.add(new Request(2, "Bilal",  1, 9)); // DHA → Cantt
        allRequests.add(new Request(3, "Fatima", 7, 9)); // Shadman → Cantt
        allRequests.add(new Request(4, "Omar",   0, 3)); // Gulberg → Model Town
        allRequests.add(new Request(5, "Zara",   4, 5)); // Bahria Town → Airport

        // Pre-bucket requests by pickup cell into a spatial HashMap
        // Room "x,y" → all requests whose pickup falls in that cell
        // O(R) one-time setup — avoids O(R) scan on every future query
        // Alternative: rebuild this every query (simpler, slower) — see no-fusion branch
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

        // Find shortest route — nodes[] passed for grid cell fusion in RouteFinder
        RouteFinder.RouteResult result = RouteFinder.findShortestRoute(
                graph.adjList, source, dest, graph.numNodes, graph.nodes);

        // result.routeCells is now ready for GridFilter (Chunk 2)
        int radius = 1; // 3×3 block — increase if too few results found
        List<Request> shortlisted = GridFilter.filter(
                requestGrid, result.routeCells, graph.nodes, radius);

        printShortListedRequests(shortlisted, radius,graph);


        printPath(result, graph);




    }
    static void printShortListedRequests(List<Request> shortlisted, int radius, Graph graph) {
        System.out.println("\n=== Shortlisted Requests (radius " + radius + ") ===");
        if (shortlisted.isEmpty()) {
            System.out.println("No nearby requests found. Try increasing radius.");
        } else {
            for (Request r : shortlisted)
                System.out.println(r.passengerName
                        + " | Pickup: "  + graph.nodes[r.pickupNode].name
                        + " → Dropoff: " + graph.nodes[r.dropOffNode].name);
        }

    }

    // Prints path with per-leg times and total journey time
    static void printPath(RouteFinder.RouteResult result, Graph graph) {
        if (result.path.isEmpty()) {
            System.out.println("No path found.");
            return;
        }

        System.out.print("Shortest path: ");
        for (int i = 0; i < result.path.size(); i++) {
            System.out.print(graph.nodes[result.path.get(i)].name);
            if (i < result.path.size() - 1)
                System.out.print(" →(" + result.legTimes.get(i) + "min)→ ");
        }
        System.out.println(" | Total: " + result.totalTime + " min");
    }
}

class Node {
    int id;
    String name;
    int x; // grid x coordinate — used later for spatial filtering
    int y; // grid y coordinate — used later for spatial filtering

    Node(int id, String name, int x, int y) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
    }
}


class Graph {
    int numNodes;
    Node[] nodes;
    Map<Integer, List<int[]>> adjList; // node ID → list of [neighborID, travelTime]

    Graph(int n) {
        this.numNodes = n;
        this.nodes    = new Node[n];
        this.adjList  = new HashMap<>();
        for (int i = 0; i < n; i++)
            adjList.put(i, new ArrayList<>());
    }

    void addNode(int id, String name, int x, int y) {
        nodes[id] = new Node(id, name, x, y);
    }

    // Undirected — adds road in both directions
    void addEdge(int from, int to, int weight) {
        adjList.get(from).add(new int[]{to, weight});
        adjList.get(to).add(new int[]{from, weight});
    }

    // O(V + E) — visits every node and every edge exactly once
    void printGraph() {
        for (int i = 0; i < numNodes; i++) {
            System.out.print(nodes[i].name + " → ");
            for (int[] edge : adjList.get(i))
                System.out.print(nodes[edge[0]].name + "(" + edge[1] + "min) ");
            System.out.println();
        }
    }
}