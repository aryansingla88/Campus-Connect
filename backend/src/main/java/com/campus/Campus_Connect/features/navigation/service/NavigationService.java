package com.campus.Campus_Connect.features.navigation.service;

import com.campus.Campus_Connect.features.navigation.dto.LatLngDto;
import com.campus.Campus_Connect.features.navigation.dto.RouteRequest;
import com.campus.Campus_Connect.features.navigation.dto.RouteResponse;
import com.campus.Campus_Connect.features.navigation.entity.DestinationNavMapping;
import com.campus.Campus_Connect.features.navigation.entity.NavNode;
import com.campus.Campus_Connect.features.navigation.repository.DestinationNavMappingRepository;
import com.campus.Campus_Connect.features.navigation.repository.NavNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NavigationService {

    private final NavNodeRepository navNodeRepository;
    private final DestinationNavMappingRepository destinationNavMappingRepository;
    private final GraphCacheService graphCacheService;
    private final DijkstraPathFinder dijkstraPathFinder;

    public RouteResponse calculateRoute(RouteRequest request) {
        NavNode startNode = navNodeRepository.findNearestNode(request.getUserLat(), request.getUserLng())
                .orElseThrow(() -> new RuntimeException("No navigation nodes found near user location."));

        DestinationNavMapping mapping = destinationNavMappingRepository
                .findByEntityTypeAndEntityId(request.getDestinationType(), request.getDestinationId())
                .orElseThrow(() -> new RuntimeException("Destination mapping not found for " + request.getDestinationType() + " ID " + request.getDestinationId()));

        Long targetNodeId = mapping.getNavNodeId();

        DijkstraPathFinder.PathResult result = dijkstraPathFinder.findShortestPath(
                startNode.getId(),
                targetNodeId,
                graphCacheService.getAdjacencyList()
        );

        List<NavNode> pathNodes = navNodeRepository.findAllById(result.nodePath);
        List<LatLngDto> pathCoordinates = new ArrayList<>();

        for (Long nodeId : result.nodePath) {
            pathNodes.stream()
                    .filter(n -> n.getId().equals(nodeId))
                    .findFirst()
                    .ifPresent(node -> pathCoordinates.add(
                            LatLngDto.builder()
                                    .latitude(node.getLocation().getY())  // PostGIS Point Y -> Lat
                                    .longitude(node.getLocation().getX()) // PostGIS Point X -> Lng
                                    .build()
                    ));
        }

        int estimatedMinutes = (int) Math.ceil(result.totalDistance / 84.0);

        return RouteResponse.builder()
                .totalDistanceMeters(Math.round(result.totalDistance * 100.0) / 100.0)
                .estimatedTimeMinutes(estimatedMinutes)
                .path(pathCoordinates)
                .build();
    }
}