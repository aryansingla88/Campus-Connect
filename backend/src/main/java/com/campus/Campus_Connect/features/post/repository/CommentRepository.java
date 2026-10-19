package com.campus.Campus_Connect.features.post.repository;

import com.campus.Campus_Connect.features.post.entity.Comment;
import com.campus.Campus_Connect.features.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByPostOrderByCreatedAtAsc(Post post);

    List<Comment> findByPostAndParentCommentIsNullOrderByCreatedAtAsc(Post post);

    boolean existsByParentComment_Id(Integer parentCommentId);

    long countByParentComment_Id(Integer parentCommentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Comment c where c.id = :commentId")
    java.util.Optional<Comment> findByIdForUpdate(@Param("commentId") Integer commentId);

    long countByPost(Post post);

    @Query("SELECT c.post.id AS postId, COUNT(c.id) AS commentCount " +
            "FROM Comment c WHERE c.post.id IN :postIds GROUP BY c.post.id")
    List<CommentCountProjection> countByPostIds(@Param("postIds") List<Integer> postIds);

    interface CommentCountProjection {
        Integer getPostId();
        Long getCommentCount();
    }

}
