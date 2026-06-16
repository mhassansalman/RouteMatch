package com.routematch;

import org.springframework.web.bind.annotation.*;
//receives match request from frontend.
@RestController
public class MatchController {

    private final RouteMatchService routeMatchService = new RouteMatchService();

    @PostMapping("/api/match")
    public RouteResponseDTO findMatches(@RequestBody MatchRequestDTO request) {
        return routeMatchService.findMatches(
                request.getSourceId(),
                request.getDestinationId()
        );
    }
}