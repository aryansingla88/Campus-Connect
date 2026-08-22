package com.campus.Campus_Connect.features.navigation.service;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class DijkstraPathFinder {

    public static class PathResult {
        public final List<Long> nodePath;
        public final Double totalDistance;

        public PathResult(List<Long> nodePath, Double totalDistance) {
            this.nodePath = nodePath;
            this.totalDistance = totalDistance;
        }
    }

    public PathResult findShortestPath(Long startNodeId, Long targetNodeId, Map<Long, List<GraphCacheService.Edge>> graph) {
        if (startNodeId.equals(targetNodeId)) {
            return new PathResult(Collections.singletonList(startNodeId), 0.0);
        }

        Map<Long, Double> distances = new HashMap<>();
        Map<Long, Long> previousNodes = new HashMap<>();
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> Double.longBitsToDouble(a[1])));

        distances.put(startNodeId, 0.0);
        pq.add(new long[]{startNodeId, Double.doubleToLongBits(0.0)});

        while (!pq.isEmpty()) {
            long[] current = pq.poll();
            long currentNode = current[0];
            double currentDist = Double.longBitsToDouble(current[1]);

            if (currentNode == targetNodeId) break;
            if (currentDist > distances.getOrDefault(currentNode, Double.MAX_VALUE)) continue;

            List<GraphCacheService.Edge> neighbors = graph.getOrDefault(currentNode, Collections.emptyList());
            for (GraphCacheService.Edge edge : neighbors) {
                double newDist = currentDist + edge.getWeight();
                if (newDist < distances.getOrDefault(edge.getTargetNodeId(), Double.MAX_VALUE)) {
                    distances.put(edge.getTargetNodeId(), newDist);
                    previousNodes.put(edge.getTargetNodeId(), currentNode);
                    pq.add(new long[]{edge.getTargetNodeId(), Double.doubleToLongBits(newDist)});
                }
            }
        }

        if (!distances.containsKey(targetNodeId)) {
            throw new RuntimeException("No route available between start and target nodes.");
        }

        List<Long> path = new LinkedList<>();
        for (Long at = targetNodeId; at != null; at = previousNodes.get(at)) {
            path.add(0, at);
        }

        return new PathResult(path, distances.get(targetNodeId));
    }
}