package com.campus.Campus_Connect.features.post.mapper;

import com.campus.Campus_Connect.features.post.dto.response.CommentResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.post.dto.response.PostResponse;
import com.campus.Campus_Connect.features.post.dto.response.PostTagResponse;
import com.campus.Campus_Connect.features.post.entity.Comment;
import com.campus.Campus_Connect.features.post.entity.Post;
import com.campus.Campus_Connect.features.post.entity.PostImage;
import com.campus.Campus_Connect.features.post.entity.PostTag;
import com.campus.Campus_Connect.features.post.entity.PostVote;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Component
public class PostMapper {

    public PostTagResponse toPostTagResponse(PostTag tag) {
        return PostTagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }

    public List<PostTagResponse> toPostTagResponseList(
            Collection<PostTag> tags) {

        return tags.stream()
                .map(this::toPostTagResponse)
                .toList();
    }

    public PostResponse toPostResponse(
            Post post,
            Integer currentUserId,
            long commentCount
    ) {

        boolean canDelete = currentUserId != null
                && currentUserId.equals(post.getCreator().getId());

        String imageUrl = post.getImages()
                .stream()
                .sorted(Comparator.comparing(PostImage::getImageOrder))
                .map(PostImage::getImageUrl)
                .findFirst()
                .orElse(null);

        int upvotes = (int) post.getVotes()
                .stream()
                .filter(v -> v.getVoteType().name().equals("UPVOTE"))
                .count();

        int downvotes = (int) post.getVotes()
                .stream()
                .filter(v -> v.getVoteType().name().equals("DOWNVOTE"))
                .count();

        var userVote = post.getVotes()
                .stream()
                .filter(v -> v.getId().getUserId().equals(currentUserId))
                .map(PostVote::getVoteType)
                .findFirst()
                .orElse(null);

        return PostResponse.builder()
                .id(post.getId())
                .postType(post.getPostType())
                .username(post.getCreator().getUsername())
                .title(post.getTitle())
                .body(post.getContentRaw())
                .tags(toPostTagResponseList(post.getTags()))
                .imageUrl(imageUrl)
                .upvotes(upvotes)
                .downvotes(downvotes)
                .userVote(userVote)
                .commentCount(commentCount)
                .createdAt(post.getCreatedAt())
                .isEdited(post.getUpdatedAt() != null && post.getCreatedAt() != null
                        && post.getUpdatedAt().isAfter(post.getCreatedAt().plusSeconds(1)))
                .canDelete(canDelete)
                .eventId(post.getEventId())
                .build();
    }

    public CommentResponse toCommentResponse(Comment comment) {

        boolean deleted = comment.getDeletedAt() != null;
        boolean canDelete = !deleted
                && comment.getCreator().getId().equals(SecurityUtils.getCurrentUserId());
        boolean canEdit = canDelete;

        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .parentCommentId(
                        comment.getParentComment() != null
                                ? comment.getParentComment().getId()
                                : null
                )
                .username(comment.getCreator().getUsername())
                .body(deleted ? "Comment deleted" : comment.getContent())
                .createdAt(comment.getCreatedAt())
                .isDeleted(deleted)
                .canDelete(canDelete)
                .canEdit(canEdit)
                .build();
    }
}
