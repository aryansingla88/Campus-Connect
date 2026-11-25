package com.campus.Campus_Connect.features.post.repository;

import com.campus.Campus_Connect.features.post.entity.Post;
import com.campus.Campus_Connect.features.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer>, JpaSpecificationExecutor<Post> {

    @Override
    @EntityGraph(attributePaths = {"creator", "creator.profile"})
    List<Post> findAll(Specification<Post> specification, Sort sort);

    @Query("select distinct p from Post p left join fetch p.images where p.id in :postIds")
    List<Post> fetchImagesForResponseByIdIn(@Param("postIds") List<Integer> postIds);

    @Query("select distinct p from Post p left join fetch p.tags where p.id in :postIds")
    List<Post> fetchTagsForResponseByIdIn(@Param("postIds") List<Integer> postIds);

    @Query("select distinct p from Post p left join fetch p.votes where p.id in :postIds")
    List<Post> fetchVotesForResponseByIdIn(@Param("postIds") List<Integer> postIds);

    @EntityGraph(attributePaths = {"creator", "creator.profile"})
    List<Post> findAllByEventIdIsNullAndVisibilityTypeIsNullAndDeletedAtIsNullOrderByCreatedAtDesc();

    List<Post> findAllByEventIdIsNotNullAndDeletedAtIsNullOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"creator", "creator.profile"})
    @Query("""
            select p from Post p
            join p.creator creator
            where p.eventId is null
              and p.visibilityType is null
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
            where p.eventId is not null
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

    boolean existsByEventIdAndDeletedAtIsNull(Integer eventId);

    @EntityGraph(attributePaths = {"creator", "creator.profile"})
    Optional<Post> findByIdAndDeletedAtIsNull(Integer postId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p where p.id = :postId")
    Optional<Post> findByIdForUpdate(@Param("postId") Integer postId);

}
