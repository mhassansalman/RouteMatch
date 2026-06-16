package com.routematch;

import java.util.*;

public class GridFilter {

    // ═══════════════════════════════════════════════════════════════
    // filter — cheap spatial pre-filter before expensive Chunk 3 scoring
    //
    // WHY: Chunk 3 runs 3 Dijkstra calls per request (A→P, P→D, D→B).
    // This filter uses O(1) HashMap lookups — no Dijkstra — to discard
    // irrelevant requests before scoring begins.
    //
    // HOW:
    // 1. Expand each route cell outward by radius → nearbyCells
    // 2. Pull requests directly from HashMap rooms for nearby cells
    //
    // COMPLEXITY:
    // Step 1: O((2r+1)² × |routeCells|) — geometry, unavoidable
    // Step 2: O(cells in radius) — independent of total request count
    // Compare: O(R) scan approach kept in no-prefilter branch
    //
    // Dropoff intentionally ignored — dropoff proximity scored in Chunk 3
    // ═══════════════════════════════════════════════════════════════
    static final int GRID_MAX = 5; // 6×6 grid, coordinates 0–5

    public static List<Request> filter(
            Map<String, List<Request>> requestGrid, // pre-bucketed by pickup cell
            Set<String> routeCells,                 // exact route cells from fusion
            Node[] nodes,
            int radius
    ) {
        // ── Step 1: Expand routeCells by radius ──────────────────
        // Nested loop is geometric — checking a square in 2D space
        // always requires two dimensions. No prior design decision
        // caused this — it is inherent to spatial expansion.
        Set<String> nearbyCells = new HashSet<>();
        nearbyCells.addAll(routeCells); // exact route cells are always nearby

        for (String cellKey : routeCells) {
            String[] parts = cellKey.split(",");
            int cx = Integer.parseInt(parts[0]);
            int cy = Integer.parseInt(parts[1]);

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {

                    // Optimization: center already added above, skip
                    if (dx == 0 && dy == 0) continue;

                    int nx = cx + dx;
                    int ny = cy + dy;

                    // Guard: skip cells outside 6×6 grid boundaries
                    if (nx >= 0 && ny >= 0 && nx <= GRID_MAX && ny <= GRID_MAX)
                        nearbyCells.add(nx + "," + ny);
                }
            }
        }

        // ── Step 2: Pull requests directly from nearby rooms ─────
        // O(cells in radius) — never scans irrelevant requests
        // requestGrid.getOrDefault returns empty list for vacant cells
        List<Request> shortlisted = new ArrayList<>();

        for (String cell : nearbyCells) {
            List<Request> inCell = requestGrid.getOrDefault(cell, Collections.emptyList());
            shortlisted.addAll(inCell);
        }

        return shortlisted;
    }
}