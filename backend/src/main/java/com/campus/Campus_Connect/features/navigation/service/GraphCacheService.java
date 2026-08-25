package com.campus.Campus_Connect.features.navigation.service;

import com.campus.Campus_Connect.features.navigation.entity.NavEdge;
import com.campus.Campus_Connect.features.navigation.repository.NavEdgeRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GraphCacheService {

    private final NavEdgeRepository navEdgeRepository;

    @Getter
    public static class Edge {
        private final Long targetNodeId;
        private final Double weight;

        public Edge(Long targetNodeId, Double weight) {
            this.targetNodeId = targetNodeId;
            this.weight = weight;
        }
    }

    @Getter
    private final Map<Long, List<Edge>> adjacencyList = new ConcurrentHashMap<>();

    @PostConstruct
    @Scheduled(fixedRate = 600000)
    public void refreshGraph() {
        List<NavEdge> edges = navEdgeRepository.findAll();
        Map<Long, List<Edge>> newGraph = new HashMap<>();

        for (NavEdge edge : edges) {
            newGraph.computeIfAbsent(edge.getFromNodeId(), k -> new ArrayList<>())
                    .add(new Edge(edge.getToNodeId(), edge.getDistanceMeters()));

            if (Boolean.TRUE.equals(edge.getIsTwoWay())) {
                newGraph.computeIfAbsent(edge.getToNodeId(), k -> new ArrayList<>())
                        .add(new Edge(edge.getFromNodeId(), edge.getDistanceMeters()));
            }
        }

        adjacencyList.clear();
        adjacencyList.putAll(newGraph);
    }
}