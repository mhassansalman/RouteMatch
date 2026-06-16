package com.routematch;

public class Request {
    int id;
    String passengerName;
    int pickupNode;   // node ID where passenger wants to be picked up
    int dropOffNode;  // node ID where passenger wants to go
    double score;     // favourability score — calculated in Chunk 3 via detour cost

    // score starts at 0 — unscored requests are never inserted into the Max-Heap
    Request(int id, String passengerName, int pickupNode, int dropOffNode) {
        this.id = id;
        this.passengerName = passengerName;
        this.pickupNode = pickupNode;
        this.dropOffNode = dropOffNode;
        this.score = 0;
    }
}