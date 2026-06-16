package com.routematch;

public class LocationDTO {
//stores one location: id and name.
    private int id;
    private String name;

    public LocationDTO(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}