package com.routematch;

import java.util.*;

public class LocationTrie {

    // One node of the Trie tree
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        List<LocationDTO> suggestions = new ArrayList<>();
    }

    private final TrieNode root = new TrieNode();

    // Insert one location name into Trie
    public void insert(LocationDTO location) {
        TrieNode current = root;
        String name = location.getName().toLowerCase();

        for (char ch : name.toCharArray()) {
            current.children.putIfAbsent(ch, new TrieNode());
            current = current.children.get(ch);
            current.suggestions.add(location);
        }
    }

    // Search locations by prefix
    public List<LocationDTO> search(String prefix) {
        if (prefix == null) return new ArrayList<>();

        TrieNode current = root;
        prefix = prefix.toLowerCase().trim();

        // Empty prefix → return all locations (first char of each name)
        if (prefix.isEmpty()) {
            List<LocationDTO> all = new ArrayList<>();
            for (TrieNode child : root.children.values()) {
                all.addAll(child.suggestions);
            }
            return all;
        }

        for (char ch : prefix.toCharArray()) {
            if (!current.children.containsKey(ch)) return new ArrayList<>();
            current = current.children.get(ch);
        }
        return current.suggestions;
    }
}