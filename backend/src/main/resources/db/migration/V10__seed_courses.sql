ALTER TABLE courses
DROP CONSTRAINT courses_course_name_key;

ALTER TABLE courses
DROP CONSTRAINT courses_course_code_key;



INSERT INTO courses (
    degree,
    program_name,
    course_code,
    degree_level,
    duration_years,
    has_branch,
    is_active
)
VALUES
-- B.Tech
('B.Tech', 'Artificial Intelligence and Machine Learning', 'AIML', 'UG', 4, TRUE, TRUE),
('B.Tech', 'Civil Engineering', 'CE', 'UG', 4, TRUE, TRUE),
('B.Tech', 'Computer Engineering', 'CSE', 'UG', 4, TRUE, TRUE),
('B.Tech', 'Electrical Engineering', 'EE', 'UG', 4, TRUE, TRUE),
('B.Tech', 'Electronics and Communication Engineering', 'ECE', 'UG', 4, TRUE, TRUE),
('B.Tech', 'Industrial Internet of Things', 'IIOT', 'UG', 4, TRUE, TRUE),
('B.Tech', 'Information Technology', 'IT', 'UG', 4, TRUE, TRUE),
('B.Tech', 'Mathematics and Computing', 'MNC', 'UG', 4, TRUE, TRUE),
('B.Tech', 'Mechanical Engineering', 'ME', 'UG', 4, TRUE, TRUE),
('B.Tech', 'Production and Industrial Engineering', 'PIE', 'UG', 4, TRUE, TRUE),


-- PG
('M.Tech', 'Civil Engineering', 'CE', 'PG', 2, TRUE, TRUE),
('M.Tech', 'Computer Engineering', 'CSE', 'PG', 2, TRUE, TRUE),
('M.Tech', 'Electrical Engineering', 'EE', 'PG', 2, TRUE, TRUE),
('M.Tech', 'Electronics and Communication Engineering', 'ECE', 'PG', 2, TRUE, TRUE),
('M.Tech', 'Mechanical Engineering', 'ME', 'PG', 2, TRUE, TRUE),
('M.Tech', 'Physics', 'PHY', 'PG', 2, FALSE, TRUE),
('M.Tech', 'School of VLSI Design and Embedded Systems', 'VLSI', 'PG', 2, FALSE, TRUE),
('M.Tech', 'School of Renewable Energy and Efficiency', 'REE', 'PG', 2, FALSE, TRUE),

('MCA', 'Master of Computer Applications', 'MCA', 'PG', 3, FALSE, TRUE),
('MBA', 'Master of Business Administration', 'MBA', 'PG', 2, FALSE, TRUE),


-- PHD
('PhD', 'Doctor of Philosophy', 'PHD', 'Doctoral', 5, FALSE, TRUE);