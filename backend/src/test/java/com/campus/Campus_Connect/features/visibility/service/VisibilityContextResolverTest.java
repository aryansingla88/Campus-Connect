package com.campus.Campus_Connect.features.visibility.service;

import com.campus.Campus_Connect.features.club.entity.ClubMember;
import com.campus.Campus_Connect.features.club.entity.ClubMemberId;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberStatus;
import com.campus.Campus_Connect.features.club.repository.ClubMemberRepository;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.metadata.hostels.entity.Hostel;
import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import com.campus.Campus_Connect.features.profile.repository.UserProfileRepository;
import com.campus.Campus_Connect.features.metadata.courses.CourseRepository;
import com.campus.Campus_Connect.features.metadata.courses.entity.Course;
import com.campus.Campus_Connect.common.academic.AcademicStatus;
import com.campus.Campus_Connect.features.visibility.domain.VisibilityContext;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VisibilityContextResolverTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-01-01T12:00:00Z"), ZoneId.of("Asia/Kolkata"));

    @Test
    void resolvesProfileAndApprovedClubIdsWithOneBulkMembershipLookup() {
        int viewerId = 18;
        UserProfile profile = UserProfile.builder()
                .userId(viewerId)
                .courseId(4)
                .admissionYear(2023)
                .hostelMetadata(Hostel.builder().id(9).build())
                .build();
        AtomicInteger profileLookups = new AtomicInteger();
        AtomicInteger membershipLookups = new AtomicInteger();
        AtomicReference<Object[]> membershipArguments = new AtomicReference<>();
        UserProfileRepository profileRepository = profileRepository(
                Optional.of(profile), profileLookups);
        ClubMemberRepository clubMemberRepository = clubMemberRepository(
                List.of(
                        ClubMember.builder().id(new ClubMemberId(2, viewerId)).build(),
                        ClubMember.builder().id(new ClubMemberId(5, viewerId)).build()
                ), membershipLookups, membershipArguments);
        VisibilityContextResolver resolver = new VisibilityContextResolver(
                profileRepository, clubMemberRepository, courseRepository(), CLOCK);

        VisibilityContext context = resolver.resolve(viewerId);

        assertEquals(viewerId, context.viewerId());
        assertEquals(4, context.courseId());
        assertEquals(9, context.hostelId());
        assertEquals("YEAR_3", context.academicStatus().apiValue());
        assertEquals(java.util.Set.of(2, 5), context.approvedClubIds());
        assertEquals(1, profileLookups.get());
        assertEquals(1, membershipLookups.get());
        assertEquals(viewerId, membershipArguments.get()[0]);
        assertEquals(ClubMemberStatus.APPROVED, membershipArguments.get()[1]);
    }

    @Test
    void missingProfileProducesAnIncompleteContextWithoutLosingClubLookup() {
        int viewerId = 21;
        AtomicInteger membershipLookups = new AtomicInteger();
        VisibilityContextResolver resolver = new VisibilityContextResolver(
                profileRepository(Optional.empty(), new AtomicInteger()),
                clubMemberRepository(List.of(), membershipLookups, new AtomicReference<>()), courseRepository(),
                CLOCK);

        VisibilityContext context = resolver.resolve(viewerId);

        assertNull(context.courseId());
        assertNull(context.hostelId());
        assertNull(context.academicStatus());
        assertEquals(java.util.Set.of(), context.approvedClubIds());
        assertEquals(1, membershipLookups.get());
    }

    @Test
    void incompleteProfileAttributesRemainUnknown() {
        int viewerId = 24;
        UserProfile incompleteProfile = UserProfile.builder().userId(viewerId).build();
        VisibilityContextResolver resolver = new VisibilityContextResolver(
                profileRepository(Optional.of(incompleteProfile), new AtomicInteger()),
                clubMemberRepository(List.of(), new AtomicInteger(), new AtomicReference<>()), courseRepository(),
                CLOCK);

        VisibilityContext context = resolver.resolve(viewerId);

        assertNull(context.courseId());
        assertNull(context.hostelId());
        assertNull(context.academicStatus());
    }

    @Test
    void currentViewerResolutionUsesTheAuthenticatedUserFromSecurityUtils() {
        int viewerId = 30;
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        User.builder().id(viewerId).build(), null, List.of()));
        VisibilityContextResolver resolver = new VisibilityContextResolver(
                profileRepository(Optional.empty(), new AtomicInteger()),
                clubMemberRepository(List.of(), new AtomicInteger(), new AtomicReference<>()), courseRepository(),
                CLOCK);

        try {
            assertEquals(viewerId, resolver.resolveCurrentViewer().viewerId());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private static CourseRepository courseRepository() {
        return proxy(CourseRepository.class, (methodName, args) -> {
            if ("findById".equals(methodName)) {
                return Optional.of(Course.builder().id(4).durationYears(4).build());
            }
            throw new UnsupportedOperationException(methodName);
        });
    }

    private static UserProfileRepository profileRepository(
            Optional<UserProfile> profile,
            AtomicInteger lookups
    ) {
        return proxy(UserProfileRepository.class, (methodName, args) -> {
            if ("findByUserIdWithUser".equals(methodName)) {
                lookups.incrementAndGet();
                return profile;
            }
            throw new UnsupportedOperationException(methodName);
        });
    }

    private static ClubMemberRepository clubMemberRepository(
            List<ClubMember> memberships,
            AtomicInteger lookups,
            AtomicReference<Object[]> arguments
    ) {
        return proxy(ClubMemberRepository.class, (methodName, args) -> {
            if ("findAllByIdUserIdAndMemberStatus".equals(methodName)) {
                lookups.incrementAndGet();
                arguments.set(args);
                return memberships;
            }
            throw new UnsupportedOperationException(methodName);
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "toString" -> type.getSimpleName() + " test proxy";
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == args[0];
                            default -> throw new UnsupportedOperationException(method.getName());
                        };
                    }
                    return invocation.invoke(method.getName(), args == null ? new Object[0] : args);
                });
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(String methodName, Object[] args);
    }
}
