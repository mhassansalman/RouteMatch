import java.util.*;

public class Main {
    public static void main(String[] args) {
        Graph graph = new Graph(10);

        graph.addNode(0, "Gulberg", 3, 4);
        graph.addNode(1, "DHA", 1, 2);
        graph.addNode(2, "Johar Town", 2, 4);
        graph.addNode(3, "Model Town", 2, 5);
        graph.addNode(4, "Bahria Town", 0, 3);
        graph.addNode(5, "Airport", 5, 1);
        graph.addNode(6, "Anarkali", 4, 6);
        graph.addNode(7, "Shadman", 3, 5);
        graph.addNode(8, "Cavalry Ground", 4, 3);
        graph.addNode(9, "Cantt", 4, 2);

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

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter source ID: ");
        int source = scanner.nextInt();
        System.out.print("Enter destination ID: ");
        int dest = scanner.nextInt();

        RouteFinder.RouteResult result = RouteFinder.findShortestRoute(graph.adjList, source, dest, graph.numNodes);
        printPath(result.path, result.shortestTimes, dest, graph);
    }

    static void printPath(List<Integer> path, int[] shortestTimes, int dest, Graph graph) {
        if (path.isEmpty()) {
            System.out.println("No path found.");
            return;
        }

        int totalTime = shortestTimes[dest]; // O(1) — no loop needed

        System.out.print("Shortest path: ");
        for (int i = 0; i < path.size(); i++) {
            System.out.print(graph.nodes[path.get(i)].name);
            if (i < path.size() - 1)
                System.out.print(" → ");
        }

        System.out.println(" | Total: " + totalTime + " min");
    }

}

class Node {
    int id;
    String name;
    int x;
    int y;

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
    Map<Integer, List<int[]>> adjList;

    Graph(int n) {
        this.numNodes = n;
        this.nodes = new Node[n];
        this.adjList = new HashMap<>();
        for (int i = 0; i < n; i++)
            adjList.put(i, new ArrayList<>());
    }

    void addNode(int id, String name, int x, int y) {
        nodes[id] = new Node(id, name, x, y);
    }

    void addEdge(int from, int to, int weight) {
        adjList.get(from).add(new int[]{to, weight});
        adjList.get(to).add(new int[]{from, weight});
    }

    void printGraph() {
        for (int i = 0; i < numNodes; i++) {
            System.out.print(nodes[i].name + " → ");
            for (int[] edge : adjList.get(i))
                System.out.print(nodes[edge[0]].name + "(" + edge[1] + "min) ");
            System.out.println();
        }
    }
}




