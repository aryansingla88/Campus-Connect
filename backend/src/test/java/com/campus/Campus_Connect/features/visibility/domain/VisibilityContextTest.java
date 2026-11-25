package com.campus.Campus_Connect.features.visibility.domain;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VisibilityContextTest {

    @Test
    void makesClubMembershipSetImmutableAndKeepsUnknownAttributesNullable() {
        Set<Integer> clubIds = new HashSet<>(Set.of(2, 7));
        VisibilityContext context = new VisibilityContext(8, null, null, null, null, clubIds);
        clubIds.add(9);

        assertEquals(Set.of(2, 7), context.approvedClubIds());
        assertThrows(UnsupportedOperationException.class, () -> context.approvedClubIds().add(10));
        assertNull(context.courseId());
        assertNull(context.hostelId());
        assertNull(context.academicYear());
        assertNull(context.alumni());
    }

    @Test
    void requiresAValidViewerId() {
        assertThrows(IllegalArgumentException.class,
                () -> new VisibilityContext(null, null, null, null, null, Set.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new VisibilityContext(0, null, null, null, null, Set.of()));
    }
}
