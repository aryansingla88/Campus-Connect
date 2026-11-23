package com.campus.Campus_Connect.features.connection.repository;

import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.connection.entity.ConnectionStatus;
import com.campus.Campus_Connect.features.connection.entity.UserConnection;
import com.campus.Campus_Connect.features.connection.entity.UserConnectionId;
import com.campus.Campus_Connect.features.connection.dto.ConnectionRelationshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<UserConnection, UserConnectionId> {

    @Query("""
            SELECT uc
            FROM UserConnection uc
            WHERE (uc.sender.id = :user1Id AND uc.receiver.id = :user2Id)
               OR (uc.sender.id = :user2Id AND uc.receiver.id = :user1Id)
            """)
    Optional<UserConnection> findConnectionBetweenUsers(
            @Param("user1Id") Integer user1Id,
            @Param("user2Id") Integer user2Id
    );

    @Query("""
            SELECT uc
            FROM UserConnection uc
            WHERE uc.status = :status
              AND (uc.sender.id = :userId OR uc.receiver.id = :userId)
            """)
    List<UserConnection> findConnectionsByUserIdAndStatus(
            @Param("userId") Integer userId,
            @Param("status") ConnectionStatus status
    );

    List<UserConnection> findByReceiver_IdAndStatus(
            Integer receiverId,
            ConnectionStatus status
    );

    List<UserConnection> findBySender_IdAndStatus(
            Integer senderId,
            ConnectionStatus status
    );

    Optional<UserConnection> findBySender_IdAndReceiver_Id(
            Integer senderId,
            Integer receiverId
    );

    @Query("""
        SELECT u
        FROM User u
        JOIN u.profile p
        WHERE u.id <> :currentUserId
          AND (
                LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
          )
        ORDER BY p.fullName
        """)
    List<User> searchUsers(
            @Param("query") String query,
            @Param("currentUserId") Integer currentUserId
    );

    @Query(value = """
        SELECT u.id AS userId,
               u.username AS username,
               p.full_name AS fullName,
               p.avatar_url AS avatarUrl,
               p.course_id AS courseId,
               p.admission_year AS admissionYear,
               CASE
                   WHEN uc.status = 'CONNECTED' THEN 'CONNECTED'
                   WHEN uc.sender_id = :currentUserId THEN 'PENDING_SENT'
                   WHEN uc.receiver_id = :currentUserId THEN 'PENDING_RECEIVED'
                   ELSE 'NOT_CONNECTED'
               END AS relationshipStatus
        FROM users u
        JOIN user_profile p ON p.user_id = u.id
        LEFT JOIN user_friends uc
          ON (uc.sender_id = :currentUserId AND uc.receiver_id = u.id)
          OR (uc.sender_id = u.id AND uc.receiver_id = :currentUserId)
        WHERE u.id <> :currentUserId
          AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(p.full_name) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY p.full_name
        LIMIT 25
        """, nativeQuery = true)
    List<UserSearchProjection> searchUsersWithRelationship(
            @Param("query") String query,
            @Param("currentUserId") Integer currentUserId
    );

    interface UserSearchProjection {
        Integer getUserId();
        String getUsername();
        String getFullName();
        String getAvatarUrl();
        Integer getCourseId();
        Integer getAdmissionYear();
        String getRelationshipStatus();
    }

    @Query("""
    SELECT COUNT(uc)
    FROM UserConnection uc
    WHERE uc.status = :status
      AND (uc.sender.id = :userId OR uc.receiver.id = :userId)
    """)
    Long countConnectionsByUserIdAndStatus(
            @Param("userId") Integer userId,
            @Param("status") ConnectionStatus status
    );
}
