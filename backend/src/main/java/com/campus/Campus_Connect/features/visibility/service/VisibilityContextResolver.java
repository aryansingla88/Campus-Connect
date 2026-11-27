package com.campus.Campus_Connect.features.visibility.service;

import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.common.academic.AcademicStatus;
import com.campus.Campus_Connect.common.academic.AcademicStatusCalculator;
import com.campus.Campus_Connect.features.club.entity.ClubMember;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberStatus;
import com.campus.Campus_Connect.features.club.repository.ClubMemberRepository;
import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import com.campus.Campus_Connect.features.profile.repository.UserProfileRepository;
import com.campus.Campus_Connect.features.metadata.courses.CourseRepository;
import com.campus.Campus_Connect.features.visibility.domain.VisibilityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Resolves viewer attributes in bulk without loading connection lists or feature resources. */
@Component
public class VisibilityContextResolver {

    private final UserProfileRepository userProfileRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final CourseRepository courseRepository;
    private final Clock clock;

    @Autowired
    public VisibilityContextResolver(
            UserProfileRepository userProfileRepository,
            ClubMemberRepository clubMemberRepository,
            CourseRepository courseRepository
    ) {
        this(userProfileRepository, clubMemberRepository, courseRepository, Clock.systemDefaultZone());
    }

    public VisibilityContextResolver(
            UserProfileRepository userProfileRepository,
            ClubMemberRepository clubMemberRepository,
            CourseRepository courseRepository,
            Clock clock
    ) {
        this.userProfileRepository = userProfileRepository;
        this.clubMemberRepository = clubMemberRepository;
        this.courseRepository = courseRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public VisibilityContext resolveCurrentViewer() {
        return resolve(SecurityUtils.getCurrentUserId());
    }

    /** The ID must come from trusted server authentication, never from a request parameter. */
    @Transactional(readOnly = true)
    public VisibilityContext resolve(Integer viewerId) {
        if (viewerId == null || viewerId <= 0) {
            throw new IllegalArgumentException("Viewer ID must be a positive integer.");
        }

        UserProfile profile = userProfileRepository.findByUserIdWithUser(viewerId).orElse(null);
        Set<Integer> approvedClubIds = clubMemberRepository
                .findAllByIdUserIdAndMemberStatus(viewerId, ClubMemberStatus.APPROVED)
                .stream()
                .map(ClubMember::getId)
                .filter(Objects::nonNull)
                .map(id -> id.getClubId())
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());

        Integer courseId = profile == null ? null : profile.getCourseId();
        Integer hostelId = profile == null || profile.getHostelMetadata() == null
                ? null
                : profile.getHostelMetadata().getId();
        AcademicStatus academicStatus = null;
        if (profile != null && courseId != null) {
            academicStatus = courseRepository.findById(courseId)
                    .map(course -> AcademicStatusCalculator.currentStatus(
                            profile.getAdmissionYear(), course.getDurationYears(), clock))
                    .orElse(null);
        }
        return new VisibilityContext(viewerId, courseId, hostelId, academicStatus, approvedClubIds);
    }
}
