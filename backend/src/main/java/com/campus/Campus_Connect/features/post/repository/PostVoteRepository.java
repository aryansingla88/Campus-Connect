package com.campus.Campus_Connect.features.post.repository;

import com.campus.Campus_Connect.features.post.entity.Post;
import com.campus.Campus_Connect.features.post.entity.PostVote;
import com.campus.Campus_Connect.features.post.entity.PostVoteId;
import com.campus.Campus_Connect.features.post.entity.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, PostVoteId>  {

    Optional<PostVote> findByPostAndIdUserId(
            Post post,
            Integer userId
    );

    long countByPostAndVoteType(Post post, VoteType voteType);
    boolean existsByPostAndIdUserId(
            Post post,
            Integer userId
    );

    void deleteByPostAndIdUserId(Post post, Integer userId);

}