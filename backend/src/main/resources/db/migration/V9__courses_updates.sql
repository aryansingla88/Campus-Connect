-- Add metadata columns to courses
ALTER TABLE courses
    ADD COLUMN degree VARCHAR(50),
    ADD COLUMN course_code VARCHAR(20) UNIQUE,
    ADD COLUMN degree_level VARCHAR(20) NOT NULL DEFAULT 'UG',
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

-- Populate existing rows (if any)
UPDATE courses
SET degree = 'MCA'
WHERE degree IS NULL;

-- Make degree mandatory
ALTER TABLE courses
    ALTER COLUMN degree SET NOT NULL;

-- Rename course_name to program_name
ALTER TABLE courses
    RENAME COLUMN course_name TO program_name;

-- Remove branch from user_profile
ALTER TABLE user_profile
DROP COLUMN branch;