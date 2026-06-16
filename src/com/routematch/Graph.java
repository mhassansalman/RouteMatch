package com.routematch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    int numNodes;
    Node[] nodes;
    Map<Integer, List<int[]>> adjList; // node ID → list of [neighborID, travelTime]

    public Graph() {
        this.adjList = new HashMap<>();
    }


    void initNodes(int count) {
        this.nodes    = new Node[count];
        this.numNodes = count;
        for (int i = 0; i < count; i++)
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
