package com.routematch;

import java.util.List;
//stores full API response: rider route + matches.
public class RouteResponseDTO {

    private String routePath;
    private int totalTime;
    private int matchCount;
    private List<MatchResultDTO> matches;

    public RouteResponseDTO(String routePath, int totalTime, int matchCount, List<MatchResultDTO> matches) {
        this.routePath = routePath;
        this.totalTime = totalTime;
        this.matchCount = matchCount;
        this.matches = matches;
    }

    public String getRoutePath()             { return routePath; }
    public int getTotalTime()                { return totalTime; }
    public int getMatchCount()               { return matchCount; }
    public List<MatchResultDTO> getMatches() { return matches; }
}