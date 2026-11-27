package com.campus.Campus_Connect.features.connection;

import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.common.academic.AcademicStatusCalculator;
import com.campus.Campus_Connect.features.metadata.courses.CourseRepository;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.auth.repository.UserRepository;
import com.campus.Campus_Connect.features.connection.dto.ConnectionRelationshipStatus;
import com.campus.Campus_Connect.features.connection.dto.ConnectionResponse;
import com.campus.Campus_Connect.features.connection.entity.ConnectionStatus;
import com.campus.Campus_Connect.features.connection.entity.UserConnection;
import com.campus.Campus_Connect.features.connection.entity.UserConnectionId;
import com.campus.Campus_Connect.features.connection.repository.ConnectionRepository;
import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public ApiResponse<List<ConnectionResponse>> getMyConnections() {

        User currentUser = SecurityUtils.getCurrentUser();


        List<UserConnection> connections =
                connectionRepository.findConnectionsByUserIdAndStatus(
                        currentUser.getId(),
                        ConnectionStatus.CONNECTED
                );

        List<ConnectionResponse> connectionResponses = new ArrayList<>();


        for (UserConnection connection : connections) {

            User otherUser = getOtherUser(connection, currentUser.getId());

            connectionResponses.add(
                    buildConnectionResponse(
                            otherUser,
                            ConnectionRelationshipStatus.CONNECTED
                    )
            );
        }

        return ApiResponse.success(
                connectionResponses,
                "Connections fetched successfully."
        );
    }
    //-----------------------
    public ApiResponse<List<ConnectionResponse>> getUserConnections(Integer userId) {

        User currentUser = SecurityUtils.getCurrentUser();

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));

        List<UserConnection> targetConnections = connectionRepository.findConnectionsByUserIdAndStatus(
                targetUser.getId(),
                ConnectionStatus.CONNECTED
        );

        List<ConnectionResponse> responses = new ArrayList<>();

        for (UserConnection connection : targetConnections) {

            User otherUser = getOtherUser(connection, targetUser.getId());

            ConnectionRelationshipStatus status = getRelationshipStatus(
                            currentUser.getId(),
                            otherUser.getId()
                    );

            responses.add(
                    buildConnectionResponse(
                            otherUser,
                            status
                    )
            );

        }
        return ApiResponse.success(
                responses,
                "Connections fetched successfully."
        );
    }
    //-----------------------
    public ApiResponse<List<ConnectionResponse>> getConnectionRequests() {

        User currentUser = SecurityUtils.getCurrentUser();

        List<ConnectionResponse> responses = new ArrayList<>();

        // Received requests
        List<UserConnection> received =
                connectionRepository.findByReceiver_IdAndStatus(
                        currentUser.getId(),
                        ConnectionStatus.PENDING
                );

        for (UserConnection connection : received) {
            responses.add(
                    buildConnectionResponse(
                            connection.getSender(),
                            ConnectionRelationshipStatus.PENDING_RECEIVED
                    )
            );
        }

        // Sent requests
        List<UserConnection> sent =
                connectionRepository.findBySender_IdAndStatus(
                        currentUser.getId(),
                        ConnectionStatus.PENDING
                );

        for (UserConnection connection : sent) {
            responses.add(
                    buildConnectionResponse(
                            connection.getReceiver(),
                            ConnectionRelationshipStatus.PENDING_SENT
                    )
            );
        }

        return ApiResponse.success(
                responses,
                "Connection requests fetched successfully."
        );
    }
    //-----------------------
    public ApiResponse<Void> sendConnectionRequest(Integer userId) {

        User currentUser = SecurityUtils.getCurrentUser();

        if (currentUser.getId().equals(userId)) {
            throw new IllegalArgumentException("You cannot send a connection request to yourself.");
        }

        User receiver = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));

        if (connectionRepository.findConnectionBetweenUsers(
                currentUser.getId(),
                receiver.getId()
        ).isPresent()) {

            throw new IllegalStateException("Connection already exists.");
        }

        UserConnection connection = new UserConnection();

        connection.setId(
                new UserConnectionId(
                        currentUser.getId(),
                        receiver.getId()
                )
        );

        connection.setSender(currentUser);
        connection.setReceiver(receiver);
        connection.setStatus(ConnectionStatus.PENDING);

        connectionRepository.save(connection);

        return ApiResponse.success(null,"Connection request sent successfully.");
    }
    //-----------------------
    public ApiResponse<Void> acceptConnectionRequest(Integer userId) {

        User currentUser = SecurityUtils.getCurrentUser();

        UserConnection connection =
                connectionRepository.findBySender_IdAndReceiver_Id(
                        userId,
                        currentUser.getId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException("Connection request not found."));

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException("Connection request not found.");
        }

        connection.setStatus(ConnectionStatus.CONNECTED);

        connectionRepository.save(connection);

        return ApiResponse.success(null,"Connection request accepted successfully.");
    }
    //-----------------------
    public ApiResponse<Void> removeConnectionRequest(Integer userId) {

        User currentUser = SecurityUtils.getCurrentUser();

        Optional<UserConnection> sent =
                connectionRepository.findBySender_IdAndReceiver_Id(
                        currentUser.getId(),
                        userId
                );

        if (sent.isPresent()) {
            connectionRepository.delete(sent.get());
            return ApiResponse.success(null,"Connection request removed successfully.");
        }

        UserConnection connection =
                connectionRepository.findConnectionBetweenUsers(
                        currentUser.getId(),
                        userId
                ).orElseThrow(() ->
                        new ResourceNotFoundException("Connection request not found."));

        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException("Connection request not found.");
        }

        connectionRepository.delete(connection);

        return ApiResponse.success(
                null,
                "Connection request removed successfully."
        );
    }
    //-----------------------
    public ApiResponse<Void> removeConnection(Integer userId) {

        User currentUser = SecurityUtils.getCurrentUser();

        UserConnection connection =
                connectionRepository.findConnectionBetweenUsers(
                        currentUser.getId(),
                        userId
                ).orElseThrow(() ->
                        new ResourceNotFoundException("Connection not found."));

        connectionRepository.delete(connection);

        return ApiResponse.success(
                null,
                "Connection removed successfully."
        );
    }
    //-----------------------
    public ApiResponse<List<ConnectionResponse>> searchUsers(String query) {

        query = query.trim();

        if (query.isEmpty()) {
            return ApiResponse.success(
                    List.of(),
                    "Users fetched successfully."
            );
        }

        User currentUser = SecurityUtils.getCurrentUser();

        List<ConnectionResponse> responses = new ArrayList<>();
        for (ConnectionRepository.UserSearchProjection user :
                connectionRepository.searchUsersWithRelationship(query, currentUser.getId())) {
            responses.add(ConnectionResponse.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .avatarUrl(user.getAvatarUrl())
                    .courseId(user.getCourseId())
                    .admissionYear(user.getAdmissionYear())
                    .academicStatus(academicStatus(user.getCourseId(), user.getAdmissionYear()))
                    .status(ConnectionRelationshipStatus.valueOf(user.getRelationshipStatus()))
                    .build());
        }

        return ApiResponse.success(
                responses,
                "Users fetched successfully."
        );
    }


//    -----------------------------------------------------------------
//
//    -----------------------------------------------------------------


    private User getOtherUser(
            UserConnection connection,
            Integer currentUserId
    ) {

        if (connection.getSender().getId().equals(currentUserId)) {
            return connection.getReceiver();
        }

        return connection.getSender();
    }
//---------------------
private ConnectionResponse buildConnectionResponse(
        User user,
        ConnectionRelationshipStatus status
) {
    UserProfile profile = user.getProfile();

    return ConnectionResponse.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .fullName(profile.getFullName())
            .avatarUrl(profile.getAvatarUrl())
            .courseId(profile.getCourseId())
            .admissionYear(profile.getAdmissionYear())
            .academicStatus(academicStatus(profile.getCourseId(), profile.getAdmissionYear()))
            .status(status)
            .build();
    }
//---------------------
    private String academicStatus(Integer courseId, Integer admissionYear) {
        if (courseId == null) return null;
        return courseRepository.findById(courseId)
                .map(course -> AcademicStatusCalculator.currentStatus(
                        admissionYear, course.getDurationYears(), java.time.Clock.systemDefaultZone()))
                .map(com.campus.Campus_Connect.common.academic.AcademicStatus::apiValue)
                .orElse(null);
    }

//---------------------
    public ConnectionRelationshipStatus getRelationshipStatus(
            Integer currentUserId,
            Integer otherUserId
    ) {

        Optional<UserConnection> connection =
                connectionRepository.findConnectionBetweenUsers(
                        currentUserId,
                        otherUserId
                );

        if (connection.isEmpty()) {
            return ConnectionRelationshipStatus.NOT_CONNECTED;
        }

        UserConnection userConnection = connection.get();

        if (userConnection.getStatus() == ConnectionStatus.CONNECTED) {
            return ConnectionRelationshipStatus.CONNECTED;
        }

        if (userConnection.getSender().getId().equals(currentUserId)) {
            return ConnectionRelationshipStatus.PENDING_SENT;
        }

        return ConnectionRelationshipStatus.PENDING_RECEIVED;
    }



}
