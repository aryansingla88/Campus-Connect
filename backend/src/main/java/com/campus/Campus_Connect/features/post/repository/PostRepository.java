package com.campus.Campus_Connect.features.post.repository;

import com.campus.Campus_Connect.features.post.entity.Post;
import com.campus.Campus_Connect.features.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    @EntityGraph(attributePaths = {"creator", "creator.profile"})
    List<Post> findAllByPostTypeAndDeletedAtIsNullOrderByCreatedAtDesc(String postType);

    List<Post> findAllByPostTypeAndEventIdIsNotNullAndDeletedAtIsNullOrderByCreatedAtDesc(String postType);

    @EntityGraph(attributePaths = {"creator", "creator.profile"})
    @Query("""
            select p from Post p
            join p.creator creator
            where p.postType = 'GENERAL'
              and p.deletedAt is null
              and (
                    lower(p.title) like lower(concat('%', :query, '%'))
                 or lower(p.contentRaw) like lower(concat('%', :query, '%'))
                 or lower(creator.username) like lower(concat('%', :query, '%'))
              )
            order by p.createdAt desc, p.id desc
            """)
    List<Post> searchGeneralPosts(@Param("query") String query);

    @EntityGraph(attributePaths = {"creator", "creator.profile"})
    @Query("""
            select p from Post p
            join p.creator creator, Event e
            where p.postType = 'EVENTS'
              and p.eventId is not null
              and p.deletedAt is null
              and e.id = p.eventId
              and (
                    lower(e.title) like lower(concat('%', :query, '%'))
                 or lower(p.contentRaw) like lower(concat('%', :query, '%'))
                 or lower(creator.username) like lower(concat('%', :query, '%'))
              )
            order by p.createdAt desc, p.id desc
            """)
    List<Post> searchEventPosts(@Param("query") String query);

    boolean existsByEventIdAndPostTypeAndDeletedAtIsNull(Integer eventId, String postType);

    @EntityGraph(attributePaths = {"creator", "creator.profile"})
    Optional<Post> findByIdAndDeletedAtIsNull(Integer postId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p where p.id = :postId")
    Optional<Post> findByIdForUpdate(@Param("postId") Integer postId);

}
