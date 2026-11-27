package com.campus.Campus_Connect.features.post.security;

import com.campus.Campus_Connect.features.post.entity.Post;
import com.campus.Campus_Connect.features.post.exception.PostNotFoundException;
import com.campus.Campus_Connect.features.post.repository.PostRepository;
import com.campus.Campus_Connect.features.visibility.domain.VisibilityContext;
import com.campus.Campus_Connect.features.visibility.service.VisibilityContextResolver;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostVisibilityAccessServiceTest {

    @Test
    void customAccessUsesCentralizedRepositorySpecification() {
        AtomicInteger contextCalls = new AtomicInteger();
        AtomicInteger visibilityQueries = new AtomicInteger();
        PostVisibilityAccessService accessService = accessService(
                true, contextCalls, visibilityQueries
        );

        assertDoesNotThrow(() -> accessService.requireCustomPostAccess(customPost()));

        org.junit.jupiter.api.Assertions.assertEquals(1, contextCalls.get());
        org.junit.jupiter.api.Assertions.assertEquals(1, visibilityQueries.get());
    }

    @Test
    void ineligibleCustomAccessIsReportedAsNotFound() {
        AtomicInteger contextCalls = new AtomicInteger();
        AtomicInteger visibilityQueries = new AtomicInteger();
        PostVisibilityAccessService accessService = accessService(
                false, contextCalls, visibilityQueries
        );

        assertThrows(PostNotFoundException.class,
                () -> accessService.requireCustomPostAccess(customPost()));
        org.junit.jupiter.api.Assertions.assertEquals(1, contextCalls.get());
        org.junit.jupiter.api.Assertions.assertEquals(1, visibilityQueries.get());
    }

    @Test
    void generalAndEventPostsKeepTheirExistingAccessBehavior() {
        AtomicInteger contextCalls = new AtomicInteger();
        AtomicInteger visibilityQueries = new AtomicInteger();
        PostVisibilityAccessService accessService = accessService(
                false, contextCalls, visibilityQueries
        );
        Post generalPost = Post.builder().id(1).visibilityType(null).build();
        Post eventPost = Post.builder().id(2).eventId(88).visibilityType("CONNECTIONS").build();

        assertDoesNotThrow(() -> accessService.requireCustomPostAccess(generalPost));
        assertDoesNotThrow(() -> accessService.requireCustomPostAccess(eventPost));

        org.junit.jupiter.api.Assertions.assertEquals(0, contextCalls.get());
        org.junit.jupiter.api.Assertions.assertEquals(0, visibilityQueries.get());
    }

    @Test
    void currentViewerCustomFeedSpecificationIsResolvedCentrally() {
        AtomicInteger contextCalls = new AtomicInteger();
        PostVisibilityAccessService accessService = accessService(false, contextCalls, new AtomicInteger());
        Specification<Post> specification = accessService.visibleCustomPostsForCurrentViewer();

        assertNotNull(specification);
        org.junit.jupiter.api.Assertions.assertEquals(1, contextCalls.get());
    }

    private static Post customPost() {
        return Post.builder()
                .id(42)
                .visibilityType("COURSE")
                .visibilityValue("12")
                .build();
    }

    private static PostVisibilityAccessService accessService(
            boolean repositoryResult,
            AtomicInteger contextCalls,
            AtomicInteger visibilityQueries
    ) {
        PostRepository repository = (PostRepository) Proxy.newProxyInstance(
                PostRepository.class.getClassLoader(),
                new Class<?>[]{PostRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("exists")
                            && args != null
                            && args.length == 1
                            && args[0] instanceof Specification<?>) {
                        visibilityQueries.incrementAndGet();
                        return repositoryResult;
                    }
                    if (method.getName().equals("toString")) return "PostRepository test proxy";
                    if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                    if (method.getName().equals("equals")) return proxy == args[0];
                    return defaultValue(method.getReturnType());
                }
        );
        VisibilityContextResolver resolver = new VisibilityContextResolver(null, null, null, java.time.Clock.systemUTC()) {
            @Override
            public VisibilityContext resolveCurrentViewer() {
                contextCalls.incrementAndGet();
                return new VisibilityContext(7, 12, 3, com.campus.Campus_Connect.common.academic.AcademicStatus.year(2), Set.of(9));
            }
        };
        return new PostVisibilityAccessService(repository, resolver);
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == void.class) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        return 0d;
    }
}
