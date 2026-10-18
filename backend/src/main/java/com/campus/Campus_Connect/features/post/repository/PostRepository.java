package com.campus.Campus_Connect.features.post.repository;

import com.campus.Campus_Connect.features.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    List<Post> findAllByPostTypeAndDeletedAtIsNullOrderByCreatedAtDesc(String postType);

    Optional<Post> findByIdAndDeletedAtIsNull(Integer postId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p where p.id = :postId")
    Optional<Post> findByIdForUpdate(@Param("postId") Integer postId);

}
