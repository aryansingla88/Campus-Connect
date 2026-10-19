package com.campus.Campus_Connect.features.post.repository;

import com.campus.Campus_Connect.features.post.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Integer> {

    Optional<PostTag> findByName(String name);

    List<PostTag> findAllByOrderByNameAsc();

    @Query(value = "SELECT pt.id AS id, pt.name AS name, " +
            "COUNT(DISTINCT p.id) AS \"postCount\" " +
            "FROM post_tags pt " +
            "LEFT JOIN post_tag_map ptm ON ptm.tag_id = pt.id " +
            "LEFT JOIN posts p ON p.id = ptm.post_id AND p.post_type = 'GENERAL' AND p.deleted_at IS NULL " +
            "GROUP BY pt.id, pt.name " +
            "ORDER BY pt.name ASC", nativeQuery = true)
    List<PostTagCountProjection> findGeneralPostCountsByTag();

    interface PostTagCountProjection {
        Integer getId();
        String getName();
        Long getPostCount();
    }

}
