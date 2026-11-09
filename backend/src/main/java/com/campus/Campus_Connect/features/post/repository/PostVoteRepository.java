package com.campus.Campus_Connect.features.post.repository;

import com.campus.Campus_Connect.features.post.entity.Post;
import com.campus.Campus_Connect.features.post.entity.PostVote;
import com.campus.Campus_Connect.features.post.entity.PostVoteId;
import com.campus.Campus_Connect.features.post.entity.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

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

    @Query("""
            select v.id.postId as postId, v.voteType as voteType, count(v) as voteCount
            from PostVote v
            where v.id.postId in :postIds
            group by v.id.postId, v.voteType
            """)
    List<VoteCountProjection> countVotesByPostIds(@Param("postIds") List<Integer> postIds);

    @Query("""
            select v from PostVote v
            where v.id.postId in :postIds and v.id.userId = :userId
            """)
    List<PostVote> findUserVotesForPosts(
            @Param("postIds") List<Integer> postIds,
            @Param("userId") Integer userId
    );

    interface VoteCountProjection {
        Integer getPostId();
        VoteType getVoteType();
        long getVoteCount();
    }

}
