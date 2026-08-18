-- V16: Rebuild courses table

-- Temporarily remove the foreign key
ALTER TABLE user_profile
DROP CONSTRAINT fk_profile_course;

-- Drop old courses table
DROP TABLE courses;

-- Recreate courses table
CREATE TABLE courses (
                         id SERIAL PRIMARY KEY,

                         program VARCHAR(255) NOT NULL UNIQUE,
                         degree VARCHAR(50) NOT NULL,
                         course_code VARCHAR(20),

                         degree_level VARCHAR(20) NOT NULL,
                         duration_years INTEGER NOT NULL,

                         CONSTRAINT uq_course_degree_code
                             UNIQUE (degree, course_code)
);

-- Fresh seed
INSERT INTO courses (
    program,
    degree,
    course_code,
    degree_level,
    duration_years
)
VALUES
    -- B.Tech
    ('B.Tech Artificial Intelligence and Machine Learning', 'B.Tech', 'AIML', 'UG', 4),
    ('B.Tech Civil Engineering', 'B.Tech', 'CE', 'UG', 4),
    ('B.Tech Computer Engineering', 'B.Tech', 'CSE', 'UG', 4),
    ('B.Tech Electrical Engineering', 'B.Tech', 'EE', 'UG', 4),
    ('B.Tech Electronics and Communication Engineering', 'B.Tech', 'ECE', 'UG', 4),
    ('B.Tech Industrial Internet of Things', 'B.Tech', 'IIOT', 'UG', 4),
    ('B.Tech Information Technology', 'B.Tech', 'IT', 'UG', 4),
    ('B.Tech Mathematics and Computing', 'B.Tech', 'MNC', 'UG', 4),
    ('B.Tech Mechanical Engineering', 'B.Tech', 'ME', 'UG', 4),
    ('B.Tech Production and Industrial Engineering', 'B.Tech', 'PIE', 'UG', 4),

    -- M.Tech
    ('M.Tech Civil Engineering', 'M.Tech', 'CE', 'PG', 2),
    ('M.Tech Computer Engineering', 'M.Tech', 'CSE', 'PG', 2),
    ('M.Tech Electrical Engineering', 'M.Tech', 'EE', 'PG', 2),
    ('M.Tech Electronics and Communication Engineering', 'M.Tech', 'ECE', 'PG', 2),
    ('M.Tech Mechanical Engineering', 'M.Tech', 'ME', 'PG', 2),
    ('M.Tech Physics', 'M.Tech', 'PHY', 'PG', 2),
    ('M.Tech School of VLSI Design and Embedded Systems', 'M.Tech', 'VLSI', 'PG', 2),
    ('M.Tech School of Renewable Energy and Efficiency', 'M.Tech', 'REE', 'PG', 2),

    -- MCA / MBA
    ('Masters of Computer Application (M.C.A.)', 'MCA', NULL, 'PG', 3),
    ('Master of Business Administration (M.B.A.)', 'MBA', NULL, 'PG', 2),

    -- PhD
    ('Doctor of Philosophy', 'PHD', NULL, 'Doctoral', 5);

-- Restore the foreign key
ALTER TABLE user_profile
    ADD CONSTRAINT fk_profile_course
        FOREIGN KEY (course_id)
            REFERENCES courses(id);