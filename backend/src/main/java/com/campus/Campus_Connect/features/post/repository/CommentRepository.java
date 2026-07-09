package com.campus.Campus_Connect.features.post.repository;

import com.campus.Campus_Connect.features.post.entity.Comment;
import com.campus.Campus_Connect.features.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByPostOrderByCreatedAtAsc(Post post);

    List<Comment> findByPostAndParentCommentIsNullOrderByCreatedAtAsc(Post post);

    long countByPost(Post post);

}