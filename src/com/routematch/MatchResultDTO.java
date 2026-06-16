package com.routematch;

public class MatchResultDTO {
//stores one matched request result
    private int rank;
    private String passengerName;
    private String pickup;
    private String dropoff;
    private int detourCost;
    private int score;

    public MatchResultDTO(int rank, String passengerName, String pickup, String dropoff, int detourCost, int score) {
        this.rank = rank;
        this.passengerName = passengerName;
        this.pickup = pickup;
        this.dropoff = dropoff;
        this.detourCost = detourCost;
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getPickup() {
        return pickup;
    }

    public String getDropoff() {
        return dropoff;
    }

    public int getDetourCost() {
        return detourCost;
    }

    public int getScore() {
        return score;
    }
}