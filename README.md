# RouteMatch

*Intelligent Ride Request Filter for Riders  |  DSA Course Project*

## Description

inDrive shows riders all nearby passenger requests regardless of their own route — forcing manual scrolling and judgment. RouteMatch solves this by computing the rider's optimal path using Dijkstra's algorithm, spatially pre-filtering requests via a 2D grid, scoring each by detour cost, and ranking them in a Max-Heap. The rider sees a clean ranked list — best-aligned passengers first, worst last.

## Project Summary

The rider inputs a source and destination. The system computes the optimal route using Dijkstra's algorithm on a weighted graph of Lahore's road network. All active passenger requests are then spatially pre-filtered using a 2D grid, scored by deviation from the rider's route, and ranked using a Max-Heap. The core focus is pure DSA — every data structure has a distinct, non-redundant algorithmic role. From v5 onward, the project also includes a Spring Boot REST API and a web frontend with Trie-powered autocomplete.

## Version History

| **Tag** | **What was added** |
| --- | --- |
| v1 | Graph + Dijkstra — terminal input, shortest path output |
| v2 | 2D Spatial Grid — pre-filter requests near route |
| v3 | Scoring Engine + Max-Heap ranking |
| v4 | CSV-based data loading — no DB dependency |
| v5 | Spring Boot REST API + Trie autocomplete + Web frontend |

## Tech Stack

| **Layer** | **Technology** |
| --- | --- |
| Language | Java |
| Data | CSV files (nodes.csv, edges.csv, requests.csv) |
| Backend | Spring Boot REST API |
| Frontend | HTML, CSS, JavaScript |
| Core Libraries | Java Collections Framework (`PriorityQueue`, `HashMap`, `ArrayList`) |
| IDE | IntelliJ IDEA |

## Architecture

```
CSV Files (nodes, edges, requests)
    └── loaded once at startup
          │
          ▼
    Graph Builder
    (adjacency list)
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
    Spring Boot REST API
    (/api/match → JSON response)
          │
          ▼
    Web Frontend
    (Trie autocomplete + ranked results table)
```

## How to Run

### Prerequisites
- Java 17+
- Maven

### Steps

```bash
# 1. Clone the repo
git clone https://github.com/mhassansalman/RouteMatch.git
cd RouteMatch

# 2. Run the Spring Boot server
mvn spring-boot:run
```

Then open your browser at:
```
http://localhost:8081
```

The CSV files (`nodes.csv`, `edges.csv`, `requests.csv`) must be in the project root — they are already included.

---

## Data Structures

| **Structure** | **Role** | **How / Where Used** |
| --- | --- | --- |
| Graph (Adjacency List) | Road network storage | Nodes are Lahore locations, edges are roads with travel time as weights. Dijkstra runs on this to find the rider's optimal route A→B. |
| Array | Route storage | Stores the computed route as an ordered sequence of nodes [A, X1, X2 ... B] — reference path for scoring every request. |
| 2D Spatial Grid (HashMap) | Spatial pre-filtering | City divided into grid cells. Only requests whose pickup cell falls near route cells are shortlisted — avoids scoring irrelevant requests. |
| Max-Heap (Priority Queue) | Request ranking | Each shortlisted request scored by detour cost. Heap keeps best match accessible in O(log n), extracts ranked list in O(n log n). |
| Trie | Location autocomplete | Prefix tree built from all location names. Each keystroke queries the Trie in O(prefix length) — powers the type-to-search input on the frontend. |

## Algorithms

| **Algorithm** | **Purpose** | **How / Where Used** |
| --- | --- | --- |
| Dijkstra's Algorithm | Optimal route finding | Run on weighted graph to compute rider's shortest path A→B. Re-run 3× per shortlisted request to calculate exact detour cost. |
| Heap Insert / Extract-Max | Priority management | Each scored request inserted into Max-Heap in O(log n). Extract-Max returns best-aligned passenger first. |
| Spatial Grid Mapping | Efficiency filtering | Maps each location to a 2D grid cell. Reduces candidate pool to only requests near the rider's route before scoring begins. |
| Trie Prefix Search | Autocomplete | On each keystroke, traverses Trie from root following prefix characters — returns all matching location suggestions in O(prefix length). |

## Complexity

| **Operation** | **Complexity** |
| --- | --- |
| Dijkstra's algorithm | O((V + E) log V) |
| Heap insert / extract | O(log n) |
| Spatial grid lookup | O(1) per cell |
| Trie insert | O(name length) |
| Trie search | O(prefix length) |
| Overall pipeline | O((V + E) log V) — dominated by Dijkstra |

---

## Classes

### DSA

| **Class** | **Role** |
| --- | --- |
| `Graph` | Adjacency list — stores the full road network. `HashMap<Integer, List<int[]>>` where each entry is `[neighborId, travelTime]`. |
| `Node` | One location: `id`, `name`, grid coordinates `x`, `y`. |
| `Request` | One passenger request: `pickupNode`, `dropOffNode`, `score` (set by scorer). |
| `RouteFinder` | Dijkstra's algorithm — finds shortest route A→B. Also exposes `findShortestTime()` for scoring calls. Fuses grid cell collection into path reconstruction to avoid a second O(n) pass. |
| `GridFilter` | 2D spatial pre-filter — expands route cells by radius, pulls matching requests from HashMap in O(1) per cell. |
| `RequestScorer` | Scores each shortlisted request using `1 / (1 + detourCost)`. Runs 3 Dijkstra calls per request. |
| `RequestRanker` | Max-Heap — inserts all scored requests, extracts in ranked order. Best match first. |
| `LocationTrie` | Prefix Trie — built from all location names. `insert()` stores each location at every prefix node. `search(prefix)` returns all matching suggestions in O(prefix length). |

### Backend

| **Class** | **Role** |
| --- | --- |
| `RouteMatchApplication` | Spring Boot entry point — starts the server. |
| `RouteMatchService` | Bridge between API and DSA logic — loads CSV data, runs the full pipeline, builds the response. |
| `Main` | Standalone terminal runner — CSV loaders (`loadNodes`, `loadEdges`, `loadRequests`) reused by `RouteMatchService`. |

### API (Controllers)

| **Class** | **Role** |
| --- | --- |
| `MatchController` | `POST /api/match` — receives `sourceId` + `destinationId`, returns ranked match results. |
| `LocationController` | `GET /api/locations` — returns all locations. `GET /api/locations/search?prefix=` — Trie-powered prefix search. |

### DTOs

| **Class** | **Role** |
| --- | --- |
| `LocationDTO` | `id` + `name` — sent to frontend for dropdown and Trie suggestions. |
| `MatchRequestDTO` | Deserializes frontend POST body: `sourceId`, `destinationId`. |
| `MatchResultDTO` | One ranked result row: rank, passenger name, pickup, dropoff, detour cost, score. |
| `RouteResponseDTO` | Full API response: route path string, total time, match count, list of `MatchResultDTO`. |

### Frontend

| **File** | **Role** |
| --- | --- |
| `index.html` | Single page — source/destination inputs (dropdown + Trie search), results table. |
| `script.js` | Calls `/api/locations/search` on each keystroke for Trie autocomplete. Calls `/api/match` on submit. Renders ranked results. |
| `style.css` | Styling — form, table, autocomplete dropdown. |

---

*Data Structures & Algorithms in Java  |  University of Central Punjab*
