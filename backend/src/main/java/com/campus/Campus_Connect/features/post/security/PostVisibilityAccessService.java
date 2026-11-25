package com.campus.Campus_Connect.features.post.security;

import com.campus.Campus_Connect.features.post.PostClassification;
import com.campus.Campus_Connect.features.post.entity.Post;
import com.campus.Campus_Connect.features.post.exception.PostNotFoundException;
import com.campus.Campus_Connect.features.post.repository.PostRepository;
import com.campus.Campus_Connect.features.visibility.service.VisibilityContextResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/** Applies the centralized visibility query to Custom Post access paths. */
@Service
@RequiredArgsConstructor
public class PostVisibilityAccessService {

    private final PostRepository postRepository;
    private final VisibilityContextResolver visibilityContextResolver;

    public Specification<Post> visibleCustomPostsForCurrentViewer() {
        return PostVisibilityQuerySpecifications.visibleCustomPostsTo(
                visibilityContextResolver.resolveCurrentViewer()
        );
    }

    /** Event and General Post behavior stays on their existing access paths. */
    public void requireCustomPostAccess(Post post) {
        if (PostClassification.classify(post) != PostClassification.CUSTOM) {
            return;
        }

        boolean visible = postRepository.exists(PostVisibilityQuerySpecifications.visibleCustomPostById(
                post.getId(),
                visibilityContextResolver.resolveCurrentViewer()
        ));
        if (!visible) {
            throw new PostNotFoundException("Post not found with id: " + post.getId());
        }
    }
}
