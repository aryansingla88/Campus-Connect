package com.campus.Campus_Connect.features.connection;

import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.auth.repository.UserRepository;
import com.campus.Campus_Connect.features.connection.dto.ConnectionRelationshipStatus;
import com.campus.Campus_Connect.features.connection.dto.ConnectionResponse;
import com.campus.Campus_Connect.features.connection.entity.ConnectionStatus;
import com.campus.Campus_Connect.features.connection.entity.UserConnection;
import com.campus.Campus_Connect.features.connection.repository.ConnectionRepository;
import com.campus.Campus_Connect.features.metadata.courses.CourseRepository;
import com.campus.Campus_Connect.features.metadata.courses.dto.CourseResponse;
import com.campus.Campus_Connect.features.metadata.courses.entity.Course;
import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

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

        List<UserConnection> requests =
                connectionRepository.findByReceiver_IdAndStatus(
                        currentUser.getId(),
                        ConnectionStatus.PENDING
                );

        List<ConnectionResponse> responses = new ArrayList<>();

        for (UserConnection connection : requests) {

            responses.add(
                    buildConnectionResponse(
                            connection.getSender(),
                            ConnectionRelationshipStatus.PENDING_RECEIVED
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

        List<User> users = connectionRepository.searchUsers(
                query,
                currentUser.getId()
        );

        List<ConnectionResponse> responses = new ArrayList<>();

        for (User user : users) {

            ConnectionRelationshipStatus status =
                    getRelationshipStatus(
                            currentUser.getId(),
                            user.getId()
                    );

            responses.add(
                    buildConnectionResponse(
                            user,
                            status
                    )
            );
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
        Course course = getCourse(profile.getCourseId());

        return ConnectionResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(profile.getFullName())
                .avatarUrl(profile.getAvatarUrl())
                .course(buildCourseResponse(course))
                .academicYear(getAcademicYear(profile.getAdmissionYear()))
                .status(status)
                .build();
    }
//---------------------
    private Integer getAcademicYear(Integer admissionYear) {

        LocalDate today = LocalDate.now();

        int academicYear = today.getYear() - admissionYear;

        if (today.getMonthValue() >= 7) {
            academicYear++;
        }

        return academicYear;
    }
//---------------------
    private Course getCourse(Integer courseId) {

        return courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found"));
    }

    private CourseResponse buildCourseResponse(Course course) {

        return CourseResponse.builder()
                .courseId(course.getId())
                .degree(course.getDegree())
                .courseCode(course.getCourseCode())
                .build();

    }
//---------------------
    private ConnectionRelationshipStatus getRelationshipStatus(
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
