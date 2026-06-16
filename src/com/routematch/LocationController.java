package com.routematch;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class LocationController {

    private final LocationTrie trie = new LocationTrie();

    public LocationController() {
        List.of(
                new LocationDTO(0, "Gulberg"),
                new LocationDTO(1, "DHA"),
                new LocationDTO(2, "Johar Town"),
                new LocationDTO(3, "Model Town"),
                new LocationDTO(4, "Bahria Town"),
                new LocationDTO(5, "Airport"),
                new LocationDTO(6, "Anarkali"),
                new LocationDTO(7, "Shadman"),
                new LocationDTO(8, "Cavalry Ground"),
                new LocationDTO(9, "Cantt")
        ).forEach(trie::insert);
    }

    @GetMapping("/api/locations")
    public List<LocationDTO> getLocations() {
        return trie.search("");
    }

    @GetMapping("/api/locations/search")
    public List<LocationDTO> search(@RequestParam String prefix) {
        if (prefix.isBlank()) return List.of();
        return trie.search(prefix);
    }
}