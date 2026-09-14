
ALTER TABLE poi
    ADD COLUMN priority INTEGER NOT NULL DEFAULT 1;

TRUNCATE TABLE poi RESTART IDENTITY CASCADE;

INSERT INTO poi
(name, latitude, longitude, description, category, icon_type, visibility, priority)
VALUES
-- =========================================================
-- ENTRANCES
-- =========================================================

(
    'NIT Main Gate',
    29.947722,
    76.822469,
    'Main entrance to the National Institute of Technology Kurukshetra campus.',
    'ENTRANCE',
    'ENTRANCE',
    'PUBLIC',
    2
),

(
    'Sports Complex Entrance',
    29.948624162088397,
    76.8159782533698,
    'Entrance providing access to the NIT Kurukshetra sports complex.',
    'ENTRANCE',
    'ENTRANCE',
    'PUBLIC',
    2
),

(
    'Sports Complex Gate',
    29.94986968479048,
    76.8151662254837,
    'Access gate serving the sports complex and nearby athletic facilities.',
    'ENTRANCE',
    'ENTRANCE',
    'PUBLIC',
    2
),

(
    'NIT Gate',
    29.95131620719396,
    76.81533898278263,
    'Campus gate providing access to the NIT Kurukshetra campus.',
    'ENTRANCE',
    'ENTRANCE',
    'PUBLIC',
    2
),


-- =========================================================
-- BOYS HOSTELS
-- =========================================================

(
    'Boys Hostel No. 1',
    29.944705566874052,
    76.82047449006848,
    'Residential hostel accommodation for male students of NIT Kurukshetra.',
    'HOSTEL',
    'HOSTEL',
    'PUBLIC',
    3
),

(
    'Boys Hostel No. 2',
    29.944883532629316,
    76.8192075349014,
    'Residential hostel accommodation for male students.',
    'HOSTEL',
    'HOSTEL',
    'PUBLIC',
    3
),

(
    'Boys Hostel No. 3',
    29.946348902136506,
    76.81967186837669,
    'Residential hostel accommodation for male students.',
    'HOSTEL',
    'HOSTEL',
    'PUBLIC',
    3
),

(
    'Boys Hostel No. 4',
    29.947405686802885,
    76.82107562387202,
    'Residential hostel accommodation for male students.',
    'HOSTEL',
    'HOSTEL',
    'PUBLIC',
    3
),

(
    'Boys Hostel No. 5',
    29.947573253584473,
    76.81983375756016,
    'Residential hostel accommodation for male students.',
    'HOSTEL',
    'HOSTEL',
    'PUBLIC',
    3
),

(
    'Boys Hostel No. 6',
    29.945965494989007,
    76.82057313499608,
    'Residential hostel accommodation for male students.',
    'HOSTEL',
    'HOSTEL',
    'PUBLIC',
    3
),

(
    'Boys Hostel No. 7',
    29.940958577265402,
    76.81848872694646,
    'Residential hostel accommodation for male students.',
    'HOSTEL',
    'HOSTEL',
    'PUBLIC',
    3
),

(
    'Boys Hostel No. 8',
    29.941990644717468,
    76.81906133608355,
    'Residential hostel accommodation for male students.',
    'HOSTEL',
    'HOSTEL',
    'PUBLIC',
    3
),

(
    'Boys Hostel No. 9',
    29.941428301600133,
    76.81699230840148,
    'Residential hostel accommodation for male students.',
    'HOSTEL',
    'HOSTEL',
    'PUBLIC',
    3
),

(
    'Boys Hostel No. 10',
    29.943184264848774,
    76.8170673166169,
    'Residential hostel accommodation for male students.',
    'HOSTEL',
    'HOSTEL',
    'PUBLIC',
    3
),

(
    'Boys Hostel No. 11',
    29.943495232717215,
    76.81890495795916,
    'Residential hostel accommodation for male students.',
    'HOSTEL',
    'HOSTEL',
    'PUBLIC',
    3
),


-- =========================================================
-- GIRLS HOSTELS
-- =========================================================

(
    'Girls Hostel No. 1',
    29.946381528137017,
    76.8139850096289,
    'Residential hostel accommodation for female students.',
    'HOSTEL',
    'HOSTEL',
    'PUBLIC',
    3
),

(
    'Girls Hostel No. 2',
    29.946160527516824,
    76.81500969040502,
    'Residential hostel accommodation for female students.',
    'HOSTEL',
    'HOSTEL',
    'PUBLIC',
    3
),

(
    'Girls Hostel No. 3',
    29.94534243322806,
    76.81474568968103,
    'Residential hostel accommodation for female students.',
    'HOSTEL',
    'HOSTEL',
    'PUBLIC',
    3
),

(
    'Girls Hostel No. 4',
    29.945489768241348,
    76.81333171970174,
    'Residential hostel accommodation for female students.',
    'HOSTEL',
    'HOSTEL',
    'PUBLIC',
    3
),


-- =========================================================
-- HOSTEL / FITNESS FACILITIES
-- =========================================================

(
    'Boys Hostel No. 10 Gym',
    29.942400381557402,
    76.81737262903188,
    'Gymnasium facility serving students residing in the nearby hostel area.',
    'FITNESS',
    'FITNESS',
    'PUBLIC',
    2
),


-- =========================================================
-- RELIGIOUS LANDMARKS
-- =========================================================

(
    'Maa Saraswati Temple',
    29.94614162749726,
    76.82087270761411,
    'Temple and spiritual landmark located within the NIT Kurukshetra campus.',
    'LANDMARK',
    'LANDMARK',
    'PUBLIC',
    2
),

(
    'Mandir',
    29.95155271181539,
    76.81151409347785,
    'Religious landmark located within the campus area.',
    'LANDMARK',
    'LANDMARK',
    'PUBLIC',
    2
),

(
    'Shiv Temple',
    29.949791605752573,
    76.8116632707602,
    'Religious landmark dedicated to Lord Shiva.',
    'LANDMARK',
    'LANDMARK',
    'PUBLIC',
    2
),

(
    'NIT Fountain',
    29.9476568331711,
    76.81633450017979,
    'Campus fountain serving as a recognizable landmark and meeting point.',
    'LANDMARK',
    'LANDMARK',
    'PUBLIC',
    2
),


-- =========================================================
-- OPEN SPACES / NATURAL AREAS
-- =========================================================

(
    'Forest Area',
    29.943506944468606,
    76.81499496766479,
    'Green and wooded area within the NIT Kurukshetra campus.',
    'OPEN_SPACE',
    'OPEN_SPACE',
    'PUBLIC',
    2
),

(
    'Campus Lake',
    29.94362564041494,
    76.81170012893433,
    'Water body and open recreational area within the campus.',
    'OPEN_SPACE',
    'OPEN_SPACE',
    'PUBLIC',
    2
),

(
    'Forest Area North',
    29.9492712486127,
    76.82136625568431,
    'Green and wooded area located in the northern part of the campus.',
    'OPEN_SPACE',
    'OPEN_SPACE',
    'PUBLIC',
    2
),

(
    'Central Park',
    29.948143204727984,
    76.81266337898528,
    'Central green space used for recreation and relaxation.',
    'OPEN_SPACE',
    'OPEN_SPACE',
    'PUBLIC',
    2
),

(
    'Park',
    29.948152708928987,
    76.81667929111588,
    'Green recreational space within the academic campus area.',
    'OPEN_SPACE',
    'OPEN_SPACE',
    'PUBLIC',
    2
),

(
    'AE Lawns',
    29.947284345406118,
    76.8165919737079,
    'Open lawn area located near the academic and engineering blocks.',
    'OPEN_SPACE',
    'OPEN_SPACE',
    'PUBLIC',
    2
),


-- =========================================================
-- STUDENT FACILITIES
-- =========================================================

(
    'Student Activity Centre',
    29.94521246655373,
    76.81805042582542,
    'Student facility used for activities, events and campus engagement.',
    'STUDENT_FACILITY',
    'STUDENT_FACILITY',
    'PUBLIC',
    3
),

(
    'Open Air Theatre',
    29.946594697243807,
    76.81821346614032,
    'Outdoor venue used for cultural programs, performances and student events.',
    'STUDENT_FACILITY',
    'STUDENT_FACILITY',
    'PUBLIC',
    3
),

(
    'Jubilee Hall',
    29.946466826670576,
    76.81541587013179,
    'Major campus hall used for ceremonies, cultural programs and institutional events.',
    'STUDENT_FACILITY',
    'STUDENT_FACILITY',
    'PUBLIC',
    3
),


-- =========================================================
-- COMMERCIAL / MARKET / FINANCIAL
-- =========================================================

(
    'NIT Market',
    29.948790489419782,
    76.8180127479541,
    'Campus commercial area containing shops and essential student services.',
    'SHOP',
    'SHOP',
    'PUBLIC',
    3
),

(
    'Public Toilet',
    29.948734484839232,
    76.81806672897561,
    'Public sanitation facility located near the campus market area.',
    'STUDENT_FACILITY',
    'STUDENT_FACILITY',
    'PUBLIC',
    2
),

(
    'SBI Branch',
    29.94876771833039,
    76.81814699023124,
    'State Bank of India banking facility serving students, staff and campus residents.',
    'FINANCIAL',
    'FINANCIAL',
    'PUBLIC',
    2
),


-- =========================================================
-- PARKING
-- =========================================================

(
    'Parking Lot',
    29.94851795630726,
    76.81792418868656,
    'Designated vehicle parking area within the campus.',
    'PARKING',
    'PARKING',
    'PUBLIC',
    2
),

(
    'Parking Zone',
    29.951197551924245,
    76.81498928628315,
    'Designated parking zone near campus facilities.',
    'PARKING',
    'PARKING',
    'PUBLIC',
    2
),

(
    'Main Parking',
    29.94777548414136,
    76.81739292301391,
    'Main parking area serving central campus facilities.',
    'PARKING',
    'PARKING',
    'PUBLIC',
    2
),

(
    'Cycle Parking',
    29.947049853563914,
    76.81566951485065,
    'Designated parking area for bicycles.',
    'PARKING',
    'PARKING',
    'PUBLIC',
    2
),


-- =========================================================
-- ADMINISTRATION
-- =========================================================

(
    'Administration Block',
    29.948891605369514,
    76.81732828249152,
    'Main administrative building handling institutional and academic administration.',
    'ADMINISTRATION',
    'ADMINISTRATION',
    'PUBLIC',
    3
),

(
    'PWD Office',
    29.951121830085206,
    76.81184012332768,
    'Office responsible for public works and infrastructure-related services.',
    'ADMINISTRATION',
    'ADMINISTRATION',
    'PUBLIC',
    3
),

(
    'Senate Hall',
    29.94780131339405,
    76.81823735396038,
    'Institute facility used for official meetings, conferences and institutional gatherings.',
    'ADMINISTRATION',
    'ADMINISTRATION',
    'PUBLIC',
    3
),


-- =========================================================
-- HEALTH / FITNESS
-- =========================================================

(
    'NIT Gym',
    29.95010032594534,
    76.81559842129921,
    'Campus gymnasium providing fitness facilities for students and staff.',
    'FITNESS',
    'FITNESS',
    'PUBLIC',
    2
),

(
    'NIT Health Centre',
    29.950212283929638,
    76.81395486926039,
    'Campus healthcare facility providing medical services to students and staff.',
    'HEALTHCARE',
    'HEALTHCARE',
    'PUBLIC',
    2
),


-- =========================================================
-- ACCOMMODATION
-- =========================================================

(
    'Guest House',
    29.949604167414332,
    76.81417006714337,
    'Institute guest accommodation for official visitors and guests.',
    'ACCOMMODATION',
    'ACCOMMODATION',
    'PUBLIC',
    2
),


-- =========================================================
-- SPORTS
-- =========================================================

(
    'NIT Main Ground',
    29.950076126714713,
    76.81632457080482,
    'Main outdoor sports ground used for athletics and campus sporting activities.',
    'SPORTS',
    'SPORTS',
    'PUBLIC',
    3
),

(
    'Football Ground',
    29.950228606237673,
    76.81727623345958,
    'Outdoor football field used for training, matches and student sporting activities.',
    'SPORTS',
    'SPORTS',
    'PUBLIC',
    2
),

(
    'Basketball Court',
    29.949462277009243,
    76.81548723574934,
    'Outdoor basketball court available for campus sports activities.',
    'SPORTS',
    'SPORTS',
    'PUBLIC',
    2
),

(
    'Volleyball Court',
    29.948928752823914,
    76.81551466181497,
    'Outdoor volleyball court used for recreational and competitive sports.',
    'SPORTS',
    'SPORTS',
    'PUBLIC',
    2
),

(
    'Badminton Court',
    29.950741985604026,
    76.81929403442375,
    'Campus badminton court used for student sports and recreation.',
    'SPORTS',
    'SPORTS',
    'PUBLIC',
    2
),

(
    'Swimming Pool',
    29.95033021044321,
    76.81871318519529,
    'Campus swimming facility used for sports, recreation and training.',
    'SPORTS',
    'SPORTS',
    'PUBLIC',
    2
),

(
    'Badminton Court Academic Area',
    29.946976547701617,
    76.81702921793106,
    'Badminton facility located near the central academic area.',
    'SPORTS',
    'SPORTS',
    'PUBLIC',
    2
),


-- =========================================================
-- ACADEMIC BUILDINGS / DEPARTMENTS
-- =========================================================

(
    'MCA MBA Block',
    29.94494034032038,
    76.81607971305888,
    'Academic block serving postgraduate programmes including MCA and MBA.',
    'ACADEMIC',
    'ACADEMIC',
    'PUBLIC',
    3
),

(
    'Examination Hall',
    29.94719245267052,
    76.81785732504471,
    'Dedicated facility used for institute examinations and academic assessments.',
    'ACADEMIC',
    'ACADEMIC',
    'PUBLIC',
    3
),

(
    'Humanities Department',
    29.94750699432081,
    76.81766011344791,
    'Academic department supporting humanities and social science education.',
    'ACADEMIC',
    'ACADEMIC',
    'PUBLIC',
    3
),

(
    'Chemistry Department',
    29.94747390840838,
    76.81723992311852,
    'Academic department focused on chemistry education and research.',
    'ACADEMIC',
    'ACADEMIC',
    'PUBLIC',
    3
),

(
    'Physics Department',
    29.947603391123884,
    76.81680357344055,
    'Academic department focused on physics education and research.',
    'ACADEMIC',
    'ACADEMIC',
    'PUBLIC',
    3
),

(
    'Electrical Engineering Block',
    29.947066201097766,
    76.816454252253,
    'Academic block supporting electrical engineering education and activities.',
    'ACADEMIC',
    'ACADEMIC',
    'PUBLIC',
    3
),

(
    'ECE Department',
    29.947944960112803,
    76.81537436232433,
    'Academic department focused on electronics and communication engineering.',
    'ACADEMIC',
    'ACADEMIC',
    'PUBLIC',
    3
),

(
    'Mechanical Engineering Department',
    29.9467125237314,
    76.81639385166088,
    'Academic department focused on mechanical engineering education and research.',
    'ACADEMIC',
    'ACADEMIC',
    'PUBLIC',
    3
),

(
    'Computer Engineering Department',
    29.94641186770249,
    76.81621028753436,
    'Academic department focused on computer engineering education and research.',
    'ACADEMIC',
    'ACADEMIC',
    'PUBLIC',
    3
),

(
    'Applied Mechanics Block',
    29.94606465735114,
    76.81627968372854,
    'Academic block supporting teaching and research in applied mechanics.',
    'ACADEMIC',
    'ACADEMIC',
    'PUBLIC',
    3
),

(
    'Lecture Hall',
    29.944934046330058,
    76.8168889146438,
    'Academic lecture facility used for teaching and large classroom sessions.',
    'ACADEMIC',
    'ACADEMIC',
    'PUBLIC',
    3
),

(
    '5 Storey Building',
    29.949889545225,
    76.81266292994252,
    'Multi-storey institutional building within the NIT Kurukshetra campus.',
    'ACADEMIC',
    'ACADEMIC',
    'PUBLIC',
    3
),


-- =========================================================
-- TECHNICAL / WORKSHOP FACILITIES
-- =========================================================

(
    'Central Workshop',
    29.945836268218663,
    76.81717702320637,
    'Technical workshop facility supporting practical training and engineering work.',
    'ACADEMIC',
    'ACADEMIC',
    'PUBLIC',
    2
),

(
    'CAD Lab',
    29.946145892459693,
    76.81579065155194,
    'Computer-aided design laboratory supporting engineering education and practical work.',
    'ACADEMIC',
    'ACADEMIC',
    'PUBLIC',
    2
),


-- =========================================================
-- LIBRARY
-- =========================================================

(
    'NIT Library',
    29.947305599118923,
    76.8151690388244,
    'Central library providing academic resources, books and study facilities.',
    'LIBRARY',
    'LIBRARY',
    'PUBLIC',
    3
),


-- =========================================================
-- RESEARCH / INNOVATION
-- =========================================================

(
    'Innovation Cell',
    29.945312909797114,
    76.81617300523168,
    'Campus innovation initiative supporting student innovation and idea development.',
    'RESEARCH',
    'RESEARCH',
    'PUBLIC',
    2
),

(
    'Startup Cell',
    29.945846117150392,
    76.81597141770271,
    'Student-focused entrepreneurship and startup support initiative.',
    'RESEARCH',
    'RESEARCH',
    'PUBLIC',
    2
);