package com.campus.Campus_Connect.features.post.repository;

import com.campus.Campus_Connect.features.post.entity.Post;
import com.campus.Campus_Connect.features.post.entity.PostVote;
import com.campus.Campus_Connect.features.post.entity.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Integer> {

    Optional<PostVote> findByPostAndUserId(Post post, Integer userId);

    long countByPostAndVoteType(Post post, VoteType voteType);

    boolean existsByPostAndUserId(Post post, Integer userId);

    void deleteByPostAndUserId(Post post, Integer userId);

}