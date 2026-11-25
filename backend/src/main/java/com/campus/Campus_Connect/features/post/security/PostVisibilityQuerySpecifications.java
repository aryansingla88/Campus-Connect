package com.campus.Campus_Connect.features.post.security;

import com.campus.Campus_Connect.features.post.entity.Post;
import com.campus.Campus_Connect.features.visibility.domain.VisibilityContext;
import com.campus.Campus_Connect.features.visibility.domain.VisibilityScope;
import com.campus.Campus_Connect.features.visibility.query.VisibilityQueryMapping;
import com.campus.Campus_Connect.features.visibility.query.VisibilitySpecifications;
import org.springframework.data.jpa.domain.Specification;

/** Post storage adapter for the shared visibility query semantics. */
public final class PostVisibilityQuerySpecifications {

    private static final VisibilityQueryMapping<Post> MAPPING = new VisibilityQueryMapping<>(
            root -> root.get("creator").get("id"),
            root -> root.get("visibilityType"),
            root -> root.get("visibilityValue"),
            VisibilityScope::name,
            null
    );

    private PostVisibilityQuerySpecifications() {
    }

    /** Eligible, non-deleted standalone Custom posts only. */
    public static Specification<Post> visibleCustomPostsTo(VisibilityContext context) {
        Specification<Post> audienceOrCreator = VisibilitySpecifications.visibleTo(context, MAPPING)
                .or((root, query, builder) -> builder.equal(
                        MAPPING.ownerId().apply(root),
                        context.viewerId()
                ));
        return audienceOrCreator
                .and((root, query, builder) -> builder.isNull(root.get("eventId")))
                .and((root, query, builder) -> builder.isNotNull(root.get("visibilityType")))
                .and((root, query, builder) -> builder.isNull(root.get("deletedAt")));
    }

    /** Visibility predicate for a direct Custom Post lookup, retaining the soft-delete check. */
    public static Specification<Post> visibleCustomPostById(
            Integer postId,
            VisibilityContext context
    ) {
        return visibleCustomPostsTo(context)
                .and((root, query, builder) -> builder.equal(root.get("id"), postId));
    }
}
