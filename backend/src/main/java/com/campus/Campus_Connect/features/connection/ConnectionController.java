package com.campus.Campus_Connect.features.connection;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.connection.dto.ConnectionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    @GetMapping("/me/connections")
    public ApiResponse<List<ConnectionResponse>> getMyConnections() {
        return connectionService.getMyConnections();
    }

    @GetMapping("/{userId}/connections")
    public ApiResponse<List<ConnectionResponse>> getUserConnections(
            @PathVariable Integer userId
    ) {
        return connectionService.getUserConnections(userId);
    }

    @GetMapping("/me/connections/requests")
    public ApiResponse<List<ConnectionResponse>> getConnectionRequests() {
        return connectionService.getConnectionRequests();
    }

    @PostMapping("/{userId}/connections/request")
    public ApiResponse<Void> sendConnectionRequest(
            @PathVariable Integer userId
    ) {
        return connectionService.sendConnectionRequest(userId);
    }

    @PostMapping("/{userId}/connections/accept")
    public ApiResponse<Void> acceptConnectionRequest(
            @PathVariable Integer userId
    ) {
        return connectionService.acceptConnectionRequest(userId);
    }

    @DeleteMapping("/{userId}/connections/request")
    public ApiResponse<Void> removeConnectionRequest(
            @PathVariable Integer userId
    ) {
        return connectionService.removeConnectionRequest(userId);
    }

    @DeleteMapping("/{userId}/connections")
    public ApiResponse<Void> removeConnection(
            @PathVariable Integer userId
    ) {
        return connectionService.removeConnection(userId);
    }

    @GetMapping("/search")
    public ApiResponse<List<ConnectionResponse>> searchUsers(
            @RequestParam String query
    ) {
        return connectionService.searchUsers(query);
    }
}