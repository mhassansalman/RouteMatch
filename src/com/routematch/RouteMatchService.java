package com.routematch;

import java.util.*;

public class RouteMatchService {
//bridge between API and your DS logic.
    public RouteResponseDTO findMatches(int source, int dest) {

        Graph graph = new Graph();

        Main.loadNodes("nodes.csv", graph);
        Main.loadEdges("edges.csv", graph);

        List<Request> allRequests = Main.loadRequests("requests.csv");

        Map<String, List<Request>> requestGrid = new HashMap<>();

        for (Request req : allRequests) {
            Node pickup = graph.nodes[req.pickupNode];
            String cell = pickup.x + "," + pickup.y;
            requestGrid.computeIfAbsent(cell, k -> new ArrayList<>()).add(req);
        }

        RouteFinder.RouteResult result = RouteFinder.findShortestRoute(
                graph.adjList,
                source,
                dest,
                graph.numNodes,
                graph.nodes
        );

        int radius = 1;

        List<Request> shortlisted = GridFilter.filter(
                requestGrid,
                result.routeCells,
                graph.nodes,
                radius
        );

        RequestScorer.score(
                shortlisted,
                graph.adjList,
                graph.numNodes,
                source,
                dest,
                result.totalTime
        );

        List<Request> ranked = RequestRanker.rank(shortlisted);

        // Build route path string e.g. "Gulberg → Shadman → Cavalry Ground → Airport"
        StringBuilder routeBuilder = new StringBuilder();
        for (int i = 0; i < result.path.size(); i++) {
            routeBuilder.append(graph.nodes[result.path.get(i)].name);
            if (i < result.path.size() - 1)
                routeBuilder.append(" → ");
        }

        List<MatchResultDTO> response = new ArrayList<>();
        int rank = 1;

        for (Request r : ranked) {
            int detourCost = calculateDetourCost(graph, source, dest, r, result.totalTime);
            response.add(new MatchResultDTO(
                    rank++,
                    r.passengerName,
                    graph.nodes[r.pickupNode].name,
                    graph.nodes[r.dropOffNode].name,
                    detourCost,
                    (int) Math.round(r.score * 100)
            ));
        }

        return new RouteResponseDTO(
                routeBuilder.toString(),
                result.totalTime,
                shortlisted.size(),
                response
        );
    }

    private int calculateDetourCost(Graph graph, int source, int dest, Request request, int originalTime) {

        int aToPTime = RouteFinder.findShortestTime(
                graph.adjList,
                source,
                request.pickupNode,
                graph.numNodes
        );

        int pToDTime = RouteFinder.findShortestTime(
                graph.adjList,
                request.pickupNode,
                request.dropOffNode,
                graph.numNodes
        );

        int dToBTime = RouteFinder.findShortestTime(
                graph.adjList,
                request.dropOffNode,
                dest,
                graph.numNodes
        );

        return (aToPTime + pToDTime + dToBTime) - originalTime;
    }
}