package com.routematch;

public class Node {
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
