package com.campus.Campus_Connect.features.registration.constants;

public class QuickFieldMask {
    public static final long FULL_NAME     = 1L << 0;  // Bit 0
    public static final long EMAIL         = 1L << 1;  // Bit 1
    public static final long PHONE         = 1L << 2;  // Bit 2
    public static final long COURSE        = 1L << 3;  // Bit 3
    public static final long YEAR_BATCH    = 1L << 4;  // Bit 4
    public static final long ROLL_NUMBER   = 1L << 5;  // Bit 5
    public static final long HOSTEL        = 1L << 6;  // Bit 6
    public static final long HOMETOWN      = 1L << 7;  // Bit 7
    public static final long GENDER        = 1L << 8;  // Bit 8
    public static final long DATE_OF_BIRTH = 1L << 9;  // Bit 9
    public static final long GITHUB        = 1L << 10; // Bit 10
    public static final long LINKEDIN      = 1L << 11; // Bit 11

    public static final long ALL_BITS_MASK = (1L << 12) - 1; // 4095

    public static boolean isValidMask(Long mask) {
        if (mask == null) return true;
        return (mask & ~ALL_BITS_MASK) == 0;
    }
}
