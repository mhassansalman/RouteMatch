# RouteMatch

*Intelligent Ride Request Filter for **Riders  |**  DSA Course Project*

## Description

inDrive shows riders all nearby passenger requests regardless of their own route — forcing manual scrolling and judgment. RouteMatch solves this by computing the rider's optimal path using Dijkstra's algorithm, spatially pre-filtering requests via a 2D grid, scoring each by detour cost, and ranking them in a Max-Heap. The rider sees a clean ranked list — best-aligned passengers first, worst last.

## Project Summary

The rider inputs a source and destination. The system computes the optimal route using Dijkstra's algorithm on a weighted graph of Lahore's road network. All active passenger requests are then spatially pre-filtered using a 2D grid, scored by deviation from the rider's route, and ranked using a Max-Heap. The output is a terminal-displayed ranked list — best-aligned passengers first. The problem is real, the solution does not exist in the actual app, and every data structure has a distinct, non-redundant algorithmic role.

## Tech Stack

| **Layer** | **Technology** |
| --- | --- |
| Language | Java |
| Database | MySQL (via JDBC) |
| Core Libraries | Java Collections Framework (`PriorityQueue`, `HashMap`, `ArrayList`) |
| IDE | Any Java IDE (IntelliJ / Eclipse / VS Code) |

## Architecture

```
MySQL Database
    └── locations, roads, requests tables
          │
          ▼
    Graph Builder
    (adjacency list loaded from DB)
          │
          ▼
    Dijkstra's Algorithm
    (rider's optimal route A → B)
          │
          ▼
    2D Spatial Grid
    (pre-filter requests near the route)
          │
          ▼
    Scoring Engine
    (detour cost per shortlisted request)
          │
          ▼
    Max-Heap Ranker
    (best match at top)
          │
          ▼
    Terminal Output + ride_matches table
```

## Data Structures

| **Structure** | **Role** | **How / Where Used** |
| --- | --- | --- |
| Graph (Adjacency List) | Road network storage | Nodes are Lahore locations, edges are roads with travel time as weights. Dijkstra's algorithm runs on this graph to find the rider's optimal route A→B. |
| Array | Route storage | Stores the rider's computed route as an ordered sequence of nodes [A, X1, X2 ... B] — the reference path against which every passenger request is scored. |
| 2D Grid | Spatial pre-filtering | City divided into cells. Only requests whose pickup cell falls near the rider's route cells are shortlisted — avoids scoring thousands of irrelevant requests. |
| Max-Heap (Priority Queue) | Request ranking | Each shortlisted request gets a favourability score based on pickup deviation, dropoff proximity, and total detour added. Heap keeps the best match accessible in O(log n). |
| Queue | Graph traversal | BFS-based queue traversal of the graph when computing shortest detour paths for each candidate passenger request. |

## Algorithms

| **Algorithm** | **Purpose** | **How / Where Used** |
| --- | --- | --- |
| Dijkstra's Algorithm | Optimal route finding | Run on the weighted graph to compute the rider's shortest path A→B. Also re-run per shortlisted request to calculate exact detour cost. |
| Breadth-First Search (BFS) | Shortest path traversal | Queue-based BFS traversal of the graph when computing detour paths for candidate requests alongside Dijkstra. |
| Heap Insert / Extract-Max | Priority management | Each scored request inserted into Max-Heap in O(log n). Extract-Max returns the best-aligned passenger in O(1) for the rider's ranked display. |
| Spatial Grid Mapping | Efficiency filtering | Maps each location coordinate to a 2D grid cell. Reduces candidate pool from all city-wide requests to only those near the rider's route before scoring. |

## Database Schema

| **Table** | **Contents** |
| --- | --- |
| `locations` | Node ID, name, latitude, longitude, grid cell |
| `roads` | Edge ID, from_node, to_node, weight (travel time) |
| `requests` | Request ID, passenger name, pickup_location_id, dropoff_location_id, timestamp |
| `rides` | Rider source, destination, timestamp |
| `ride_matches` | Ride ID, request ID, detour cost, favourability score, rank |

## Build Order

| **Chunk** | **Steps** | **Deliverable** |
| --- | --- | --- |
| 1 — Foundation | Define nodes & edges → build adjacency list → run Dijkstra's → store route array | Working path from A to B |
| 2 — Filter | Divide city into grid → map requests to cells → compare with route cells → discard far requests | Shortlist of relevant requests only |
| 3 — Score | Re-run Dijkstra per request → compute detour cost → calculate favourability score | Every request has a score |
| 4 — Rank & Display | Insert scored requests into Max-Heap → extract in order → print ranked list → write to `ride_matches` | Full system working end to end |

## Complexity

| **Operation** | **Complexity** |
| --- | --- |
| Dijkstra's algorithm | O((V + E) log V) |
| BFS traversal | O(V + E) |
| Heap insert / extract | O(log n) |
| Spatial grid lookup | O(1) per cell |
| Overall pipeline | O((V + E) log V) — dominated by Dijkstra |

## Classes

| **Class** | **Fields** | **Role** |
| --- | --- | --- |
| `Node` | `int id`, `String name`, `double x, y` | Represents a Lahore location |
| `Edge` | `int to`, `int weight` | Represents a road between two nodes |
| `Request` | `int pickup`, `int dropoff`, `double score` | Represents a passenger ride request |
| `Graph` | `HashMap<Integer, List<Edge>>` | Stores the full road network |

---

*Data Structures **&** Algorithms in **Java  |**  University of Central Punjab*
