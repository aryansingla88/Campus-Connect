
INSERT INTO clubs (name, description, logo_url)
VALUES

-- ============================================================================
-- 12 SOCIETIES
-- ============================================================================

(
    'Technobyte',
    'A techno-managerial society for Computer Engineering students, focused on developing technical and managerial skills through technology-related learning and activities.',
    NULL
),

(
    'Mexperts',
    'A technical and managerial society for MCA and MBA students, bringing together technology and management through interdisciplinary learning, competitions, and innovation.',
    NULL
),

(
    'MechSoc',
    'The technical society for Mechanical Engineering and Industrial Engineering & Management students, focused on technical learning, engineering activities, and practical skill development.',
    NULL
),

(
    'Innovation Cell',
    'A society focused on innovation and incubation, encouraging students to develop ideas, explore new solutions, and turn creative concepts into practical initiatives.',
    NULL
),

(
    'Antariksh',
    'An astronomy society for students interested in astronomy, space, and exploring the scientific understanding of the universe.',
    NULL
),

(
    'Microbus',
    'A technical society for students interested in microcontrollers, embedded systems, and electronics-based projects.',
    NULL
),

(
    'EMR',
    'A technical society focused on embedded systems and robotic control, encouraging students to explore electronics, automation, and robotics.',
    NULL
),

(
    'Infrastructure',
    'A technical society focused on civil engineering, infrastructure, and the development of engineering knowledge and practical technical skills.',
    NULL
),

(
    'Aeromodelling Club',
    'A technical society for students interested in aeromodelling, aircraft design, and building and experimenting with model aircraft.',
    NULL
),

(
    'Startup Cell',
    'A society focused on entrepreneurship and startup development, encouraging students to explore ideas, innovation, and the process of building new ventures.',
    NULL
),

(
    'Electroreck',
    'The Electrical Engineering technical society, focused on developing students’ technical knowledge through engineering activities, competitions, and interactive learning.',
    NULL
),

(
    'Anant',
    'A student society focused on technical learning, innovation, and collaborative activities.',
    NULL
),

-- ============================================================================
-- 11 CLUBS
-- ============================================================================

(
    'Photography Club',
    'A creative club focused on photography, videography, editing, and visual storytelling, while documenting institute events and campus life.',
    NULL
),

(
    'Managing and Directing Club',
    'A student club focused on managing and directing institute activities, helping students develop coordination, leadership, and event-management skills.',
    NULL
),

(
    'Audio and Visual Aid Club',
    'A club responsible for sound and lighting during institute events, while helping students develop skills in audio, visual, and music production.',
    NULL
),

(
    'SPICMACAY x MCC Club',
    'A cultural club promoting Indian art, music, dance, and heritage, while bringing students together through cultural activities and events.',
    NULL
),

(
    'Colours',
    'A creative club providing students with opportunities to explore artistic expression, creativity, and visual arts.',
    NULL
),

(
    'Siksha',
    'A student club focused on learning, academic development, and educational activities.',
    NULL
),

(
    'Hindi Literary and Debating',
    'A literary and debating club for Hindi language, writing, public speaking, and discussions.',
    NULL
),

(
    'English Literary and Debating',
    'A literary and debating club that organises literary, oratory, and quizzing activities while helping students develop communication skills.',
    NULL
),

(
    'Post Graduate Club',
    'A club for postgraduate students, helping them connect across programmes and participate in institute activities, events, and major fests.',
    NULL
),

(
    'Hiking and Trekking Club',
    'An outdoor club for students interested in hiking, trekking, exploration, and adventure activities.',
    NULL
),

(
    'Fine Arts',
    'A creative club that promotes art and craft, providing students opportunities to develop their artistic skills and contribute to the visual decoration of institute events.',
    NULL
)

    ON CONFLICT (name) DO UPDATE
                              SET
                                  description = EXCLUDED.description,
                              logo_url = EXCLUDED.logo_url;