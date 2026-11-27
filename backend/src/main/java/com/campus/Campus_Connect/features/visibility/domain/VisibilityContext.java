package com.campus.Campus_Connect.features.visibility.domain;

import com.campus.Campus_Connect.common.academic.AcademicStatus;
import java.util.Set;

/** Compact, database-independent attributes of the viewer used by future visibility evaluation. */
public record VisibilityContext(
        Integer viewerId,
        Integer courseId,
        Integer hostelId,
        AcademicStatus academicStatus,
        Set<Integer> approvedClubIds
) {
    public VisibilityContext {
        if (viewerId == null || viewerId <= 0) {
            throw new IllegalArgumentException("Viewer ID must be a positive integer.");
        }
        approvedClubIds = approvedClubIds == null ? Set.of() : Set.copyOf(approvedClubIds);
    }
}
