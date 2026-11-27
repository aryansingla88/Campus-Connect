-- Test fixture for registered testuser01 through testuser10.
-- Run the whole script in pgAdmin. It is safe to rerun.
BEGIN;

-- Stop with a useful message if an account or required course is missing.
DO $$
DECLARE missing_users TEXT;
BEGIN
    SELECT string_agg(x.username, ', ' ORDER BY x.username)
    INTO missing_users
    FROM (VALUES ('testuser01'), ('testuser02'), ('testuser03'), ('testuser04'),
                 ('testuser05'), ('testuser06'), ('testuser07'), ('testuser08'),
                 ('testuser09'), ('testuser10')) AS x(username)
    LEFT JOIN users u ON u.username = x.username
    WHERE u.id IS NULL;

    IF missing_users IS NOT NULL THEN
        RAISE EXCEPTION 'Register these users first: %', missing_users;
    END IF;

    IF (SELECT count(*) FROM courses WHERE course_code IN ('CSE','IT','AIML','ECE') AND degree='B.Tech') < 4 THEN
        RAISE EXCEPTION 'Required B.Tech courses CSE, IT, AIML, and ECE were not found.';
    END IF;
END $$;

-- Profiles and avatars. Existing profile rows are refreshed on rerun.
INSERT INTO user_profile
    (user_id, full_name, bio, avatar_url, course_id, admission_year, roll_number, hostel, hometown, phone)
SELECT u.id, v.full_name, v.bio, v.avatar_url, c.id, v.admission_year,
       v.roll_number, v.hostel, v.hometown, v.phone
FROM (VALUES
    ('testuser01','Test User 01','Builds small tools for campus life.','https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=512&q=80','CSE',2023,'TEST0001','Block A','Pune','+91 90000 10001'),
    ('testuser02','Test User 02','Interested in product design and student communities.','https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=512&q=80','IT',2023,'TEST0002','Block B','Mumbai','+91 90000 10002'),
    ('testuser03','Test User 03','Learning backend development and cloud systems.','https://images.unsplash.com/photo-1531123897727-8f129e1688ce?auto=format&fit=crop&w=512&q=80','AIML',2022,'TEST0003','Block C','Bengaluru','+91 90000 10003'),
    ('testuser04','Test User 04','Photographer and weekend trekker.','https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=512&q=80','ECE',2022,'TEST0004','Block D','Hyderabad','+91 90000 10004'),
    ('testuser05','Test User 05','Exploring mobile apps and community projects.','https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=512&q=80','CSE',2021,'TEST0005','Block E','Delhi','+91 90000 10005'),
    ('testuser06','Test User 06','Music, accessibility, and clean interface design.','https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=512&q=80','IT',2021,'TEST0006','Block F','Chennai','+91 90000 10006'),
    ('testuser07','Test User 07','Interested in robotics and embedded systems.','https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=512&q=80','ECE',2023,'TEST0007','Block A','Jaipur','+91 90000 10007'),
    ('testuser08','Test User 08','Writes, designs, and helps teams tell their story.','https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=512&q=80','AIML',2022,'TEST0008','Block B','Kochi','+91 90000 10008'),
    ('testuser09','Test User 09','Interested in data science and climate tech.','https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=512&q=80','CSE',2021,'TEST0009','Block C','Pune','+91 90000 10009'),
    ('testuser10','Test User 10','Learning full-stack development with friends.','https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=512&q=80','IT',2023,'TEST0010','Block D','Mumbai','+91 90000 10010')
) AS v(username,full_name,bio,avatar_url,course_code,admission_year,roll_number,hostel,hometown,phone)
JOIN users u ON u.username=v.username
JOIN courses c ON c.course_code=v.course_code AND c.degree='B.Tech'
ON CONFLICT (user_id) DO UPDATE SET
    full_name=EXCLUDED.full_name, bio=EXCLUDED.bio, avatar_url=EXCLUDED.avatar_url,
    course_id=EXCLUDED.course_id, admission_year=EXCLUDED.admission_year,
    roll_number=EXCLUDED.roll_number, hostel=EXCLUDED.hostel,
    hometown=EXCLUDED.hometown, phone=EXCLUDED.phone, updated_at=CURRENT_TIMESTAMP;

-- Twelve events. testuser01 creates two, testuser02 creates three, and
-- testuser03 through testuser09 create one each. testuser10 attends only.
INSERT INTO events
    (title,description,creator_id,club_id,host_name,venue,latitude,longitude,
     start_time,end_time,visibility_type,registration_type,approval_status,event_state,
     base_priority_level,priority_score,priority_level)
SELECT v.title,v.description,u.id,c.id,v.club_name,v.venue,29.9482,76.8190,
       CURRENT_TIMESTAMP + make_interval(days => v.day_offset),
       CURRENT_TIMESTAMP + make_interval(days => v.day_offset, hours => v.duration_hours),
       'PUBLIC','THROUGH_APP','VERIFIED','UPCOMING',2,0,2
FROM (VALUES
    ('Campus Hackathon 01','A team hackathon for useful campus tools.','testuser01','Technobyte','Innovation Lab',7,4),
    ('Mobile App Workshop 02','Hands-on mobile app prototyping.','testuser02','Technobyte','Engineering Block A',8,3),
    ('Ideas to Impact 03','Student founders share product lessons.','testuser03','Innovation Cell','Innovation Cell Hall',9,2),
    ('Green Campus Sprint 04','Design ideas for a more sustainable campus.','testuser04','Innovation Cell','Central Library',10,4),
    ('Research Poster Walk 05','Explore student research and meet presenters.','testuser05','Technobyte','Jubilee Hall',11,3),
    ('Founder Fireside 06','An informal conversation with student founders.','testuser06','Innovation Cell','Innovation Cell Hall',12,2),
    ('Badminton Night 07','Friendly campus badminton and social evening.','testuser07','Technobyte','Sports Complex',13,2),
    ('AI Society Meetup 08','Meet students working on AI projects.','testuser08','Technobyte','Engineering Block B',14,2),
    ('Photography Walk 09','A guided photo walk around campus.','testuser09','Innovation Cell','Main Gate',15,2),
    ('Full Stack Study Jam 10','Collaborative full stack coding session.','testuser01','Technobyte','Computer Lab 1',16,3),
    ('Cloud Systems Lab 11','A practical introduction to cloud deployment.','testuser02','Technobyte','Computer Lab 2',17,3),
    ('Student Project Expo 12','Showcase projects built by campus teams.','testuser02','Innovation Cell','Jubilee Hall',18,4)
) AS v(title,description,creator_username,club_name,venue,day_offset,duration_hours)
JOIN users u ON u.username=v.creator_username
JOIN clubs c ON c.name=v.club_name
WHERE NOT EXISTS (
    SELECT 1 FROM events existing
    WHERE existing.title=v.title AND existing.creator_id=u.id
);

-- A poster for every fixture event; skip it if already attached.
INSERT INTO event_posters (event_id,poster_url)
SELECT e.id,v.poster_url
FROM (VALUES
    ('Campus Hackathon 01','https://images.unsplash.com/photo-1504384308090-c894fdcc538d?auto=format&fit=crop&w=1200&q=80'),
    ('Mobile App Workshop 02','https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80'),
    ('Ideas to Impact 03','https://images.unsplash.com/photo-1556761175-b413da4baf72?auto=format&fit=crop&w=1200&q=80'),
    ('Green Campus Sprint 04','https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=1200&q=80'),
    ('Research Poster Walk 05','https://images.unsplash.com/photo-1531058020387-3be344556be6?auto=format&fit=crop&w=1200&q=80'),
    ('Founder Fireside 06','https://images.unsplash.com/photo-1511795409834-ef04bbd61622?auto=format&fit=crop&w=1200&q=80'),
    ('Badminton Night 07','https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?auto=format&fit=crop&w=1200&q=80'),
    ('AI Society Meetup 08','https://images.unsplash.com/photo-1485827404703-89b55fcc595e?auto=format&fit=crop&w=1200&q=80'),
    ('Photography Walk 09','https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=1200&q=80'),
    ('Full Stack Study Jam 10','https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80'),
    ('Cloud Systems Lab 11','https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=1200&q=80'),
    ('Student Project Expo 12','https://images.unsplash.com/photo-1521737711867-e3b97375f902?auto=format&fit=crop&w=1200&q=80')
) AS v(title,poster_url)
JOIN events e ON e.title=v.title
JOIN users creator ON creator.id=e.creator_id
WHERE creator.username IN ('testuser01','testuser02','testuser03','testuser04','testuser05','testuser06','testuser07','testuser08','testuser09','testuser10')
  AND NOT EXISTS (SELECT 1 FROM event_posters p WHERE p.event_id=e.id);

-- Add creator memberships plus selected admin/volunteer access for other users.
-- EventMemberRole supports CREATOR, ADMIN, and VOLUNTEER in this schema.
INSERT INTO event_members (event_id,user_id,role)
SELECT e.id,u.id,
       CASE WHEN u.id=e.creator_id THEN 'CREATOR'
            WHEN (u.username='testuser02' AND e.title IN ('Campus Hackathon 01','Ideas to Impact 03','AI Society Meetup 08'))
              OR (u.username='testuser05' AND e.title IN ('Mobile App Workshop 02','Green Campus Sprint 04','Student Project Expo 12'))
              THEN 'ADMIN'
            ELSE 'VOLUNTEER' END
FROM events e
JOIN users creator ON creator.id=e.creator_id
CROSS JOIN users u
WHERE e.title IN ('Campus Hackathon 01','Mobile App Workshop 02','Ideas to Impact 03','Green Campus Sprint 04',
                  'Research Poster Walk 05','Founder Fireside 06','Badminton Night 07','AI Society Meetup 08',
                  'Photography Walk 09','Full Stack Study Jam 10','Cloud Systems Lab 11','Student Project Expo 12')
  AND creator.username IN ('testuser01','testuser02','testuser03','testuser04','testuser05','testuser06','testuser07','testuser08','testuser09','testuser10')
  AND u.username IN ('testuser01','testuser02','testuser03','testuser04','testuser05','testuser06','testuser07','testuser08','testuser09','testuser10')
  AND (u.id=e.creator_id OR u.username IN ('testuser02','testuser05','testuser08'))
ON CONFLICT (event_id,user_id) DO NOTHING;

COMMIT;
