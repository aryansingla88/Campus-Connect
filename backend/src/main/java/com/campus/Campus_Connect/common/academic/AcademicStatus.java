package com.campus.Campus_Connect.common.academic;

/** Normalized academic status for a student's current academic session. */
public record AcademicStatus(Integer academicYear, boolean alumni) {
    public AcademicStatus {
        if (alumni && academicYear != null) {
            throw new IllegalArgumentException("Alumni status cannot also have a numeric year.");
        }
        if (!alumni && (academicYear == null || academicYear < 1)) {
            throw new IllegalArgumentException("A student status requires a positive academic year.");
        }
    }

    public static AcademicStatus year(int year) {
        return new AcademicStatus(year, false);
    }

    public static AcademicStatus alumniStatus() {
        return new AcademicStatus(null, true);
    }

    /** Stable API representation, e.g. YEAR_5 or ALUMNI. */
    public String apiValue() {
        return alumni ? "ALUMNI" : "YEAR_" + academicYear;
    }

    /** Numeric audience value used by the existing Year 1–4 post contract. */
    public String visibilityAudienceValue() {
        return alumni ? "ALUMNI" : Integer.toString(academicYear);
    }

    public String displayLabel() {
        if (alumni) return "Alumni";
        int year = academicYear;
        int lastTwo = year % 100;
        String suffix = (lastTwo >= 11 && lastTwo <= 13) ? "th" : switch (year % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
        return year + suffix + " Year";
    }
}
