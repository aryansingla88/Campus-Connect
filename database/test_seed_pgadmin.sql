-- Campus Connect local test fixture. Run after Flyway and after Postman has
-- created testuser01..testuser12. Idempotent for the fixture-owned rows.
BEGIN;

-- Fail early if Postman registration has not completed.
DO $$
BEGIN
  IF (SELECT count(*) FROM users WHERE username ~ '^testuser(0[1-9]|1[0-2])$') < 12 THEN
    RAISE EXCEPTION 'Expected testuser01 through testuser12. Run their Postman registration requests first.';
  END IF;
END $$;

-- Refresh profile examples for the first 12 accounts; user rows and passwords
-- remain untouched. Image URLs are stored as URLs, not downloaded by this SQL.
UPDATE user_profile p SET
  bio=x.bio, avatar_url=x.avatar_url, hometown=x.hometown, hostel=x.hostel,
  phone=x.phone, github=x.github, linkedin=x.linkedin, instagram=x.instagram
FROM (VALUES
 ('testuser01','Builds small tools for campus life.','https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=512&q=80','Pune','Block A','+91 90000 10001','https://github.com/testuser01','https://www.linkedin.com/in/testuser01','https://www.instagram.com/testuser01'),
 ('testuser02','Interested in product design and student communities.','https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=512&q=80','Mumbai','Block B','+91 90000 10002','https://github.com/testuser02','https://www.linkedin.com/in/testuser02','https://www.instagram.com/testuser02'),
 ('testuser03','Learning backend development and cloud systems.','https://images.unsplash.com/photo-1531123897727-8f129e1688ce?auto=format&fit=crop&w=512&q=80','Bengaluru','Block C','+91 90000 10003','https://github.com/testuser03','https://www.linkedin.com/in/testuser03','https://www.instagram.com/testuser03'),
 ('testuser04','Photographer and weekend trekker.','https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=512&q=80','Hyderabad','Block D','+91 90000 10004','https://github.com/testuser04','https://www.linkedin.com/in/testuser04','https://www.instagram.com/testuser04'),
 ('testuser05','Exploring mobile apps and community projects.','https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=512&q=80','Delhi','Block E','+91 90000 10005','https://github.com/testuser05','https://www.linkedin.com/in/testuser05','https://www.instagram.com/testuser05'),
 ('testuser06','Music, accessibility, and clean interface design.','https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=512&q=80','Chennai','Block F','+91 90000 10006','https://github.com/testuser06','https://www.linkedin.com/in/testuser06','https://www.instagram.com/testuser06'),
 ('testuser07','Interested in robotics and embedded systems.','https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=512&q=80','Jaipur','Block A','+91 90000 10007','https://github.com/testuser07','https://www.linkedin.com/in/testuser07','https://www.instagram.com/testuser07'),
 ('testuser08','Writes, designs, and helps teams tell their story.','https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=512&q=80','Kochi','Block B','+91 90000 10008','https://github.com/testuser08','https://www.linkedin.com/in/testuser08','https://www.instagram.com/testuser08'),
 ('testuser09','Interested in data science and climate tech.','https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=512&q=80','Pune','Block C','+91 90000 10009','https://github.com/testuser09','https://www.linkedin.com/in/testuser09','https://www.instagram.com/testuser09'),
 ('testuser10','Learning full-stack development with friends.','https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=512&q=80','Mumbai','Block D','+91 90000 10010','https://github.com/testuser10','https://www.linkedin.com/in/testuser10','https://www.instagram.com/testuser10'),
 ('testuser11','Enjoys organizing workshops and hack nights.','https://images.unsplash.com/photo-1531123897727-8f129e1688ce?auto=format&fit=crop&w=512&q=80','Bengaluru','Block E','+91 90000 10011','https://github.com/testuser11','https://www.linkedin.com/in/testuser11','https://www.instagram.com/testuser11'),
 ('testuser12','Curious about startups, research, and product strategy.','https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=512&q=80','Hyderabad','Block F','+91 90000 10012','https://github.com/testuser12','https://www.linkedin.com/in/testuser12','https://www.instagram.com/testuser12')
) x(username,bio,avatar_url,hometown,hostel,phone,github,linkedin,instagram)
JOIN users u ON u.username=x.username WHERE p.user_id=u.id;

-- Preferences and presence for the test accounts.
INSERT INTO user_preferences(user_id)
SELECT id FROM users WHERE username ~ '^testuser(0[1-9]|1[0-2])$'
ON CONFLICT (user_id) DO NOTHING;
UPDATE user_preferences p SET show_phone=TRUE,show_socials=TRUE,show_presence='PUBLIC',theme='dark'
FROM users u WHERE u.id=p.user_id AND u.username='testuser01';

INSERT INTO user_presence(user_id, latitude, longitude)
SELECT id, 29.9478 + ((id % 7) * 0.0003), 76.8190 + ((id % 9) * 0.0003)
FROM users WHERE username ~ '^testuser(0[1-9]|1[0-2])$'
ON CONFLICT (user_id) DO UPDATE SET latitude=EXCLUDED.latitude, longitude=EXCLUDED.longitude,
  last_updated=CURRENT_TIMESTAMP;

-- Attach a sample shop to the Flyway-seeded market POI; keep the POI seed row intact.
INSERT INTO shops(poi_id,opening_time,closing_time,phone_number)
SELECT id,TIME '08:00',TIME '22:00','01744-000123' FROM poi WHERE name='NIT Market'
ON CONFLICT(poi_id) DO NOTHING;

-- Interests and user-interest links.
INSERT INTO interests(label, category) VALUES
 ('Software Development','Technology'),('Robotics','Technology'),
 ('Entrepreneurship','Business'),('Photography','Arts'),
 ('Music','Arts'),('Sustainability','Community'),('Public Speaking','Career')
ON CONFLICT(label) DO NOTHING;

INSERT INTO user_interests(user_id, interest_id)
SELECT u.id, i.id FROM users u JOIN interests i ON i.label = CASE
  WHEN u.id % 7 = 0 THEN 'Software Development'
  WHEN u.id % 7 = 1 THEN 'Robotics'
  WHEN u.id % 7 = 2 THEN 'Entrepreneurship'
  WHEN u.id % 7 = 3 THEN 'Photography'
  WHEN u.id % 7 = 4 THEN 'Music'
  WHEN u.id % 7 = 5 THEN 'Sustainability'
  ELSE 'Public Speaking' END
WHERE u.username ~ '^testuser(0[1-9]|1[0-2])$'
ON CONFLICT DO NOTHING;
INSERT INTO user_interests(user_id,interest_id)
SELECT u.id,i.id FROM users u CROSS JOIN interests i
WHERE u.username='testuser01'
ON CONFLICT(user_id,interest_id) DO NOTHING;

-- Connected people and pending requests centered on testuser01.
INSERT INTO user_friends(sender_id,receiver_id,status)
SELECT a.id,b.id,x.status FROM (VALUES
 ('testuser01','testuser02','CONNECTED'),('testuser01','testuser03','CONNECTED'),
 ('testuser01','testuser04','PENDING'),('testuser05','testuser01','PENDING'),
 ('testuser01','testuser06','CONNECTED'),('testuser01','testuser07','CONNECTED'),
 ('testuser08','testuser01','CONNECTED'),('testuser01','testuser09','CONNECTED'),
 ('testuser10','testuser01','PENDING'),('testuser01','testuser11','CONNECTED'),
 ('testuser12','testuser01','CONNECTED'),('testuser02','testuser03','CONNECTED'),
 ('testuser06','testuser07','CONNECTED'),('testuser09','testuser10','CONNECTED')
) x(sender_name,receiver_name,status)
JOIN users a ON a.username=x.sender_name JOIN users b ON b.username=x.receiver_name
ON CONFLICT(sender_id,receiver_id) DO NOTHING;

-- Join two existing Flyway-seeded clubs; do not recreate or edit club seeds.
INSERT INTO club_members(club_id, user_id, mem_status, role, joined_at)
SELECT c.id, u.id, 'APPROVED', 'member', CURRENT_TIMESTAMP - ((u.id % 30) || ' days')::interval
FROM clubs c CROSS JOIN users u
WHERE c.name IN ('Technobyte','Innovation Cell') AND u.username ~ '^testuser(0[1-9]|1[0-2])$'
  AND u.username <> 'testuser01' AND (u.id % 2 = 0 OR c.name='Technobyte')
ON CONFLICT(club_id, user_id) DO NOTHING;
INSERT INTO club_members(club_id,user_id,mem_status,role,joined_at)
SELECT c.id,u.id,'APPROVED','PRESIDENT',CURRENT_TIMESTAMP - interval '90 days'
FROM clubs c JOIN users u ON u.username='testuser01' WHERE c.name='Technobyte'
ON CONFLICT(club_id,user_id) DO UPDATE SET mem_status='APPROVED',role='PRESIDENT';
INSERT INTO club_members(club_id,user_id,mem_status,role,joined_at)
SELECT c.id,u.id,'PENDING','MEMBER',NULL
FROM clubs c JOIN users u ON u.username='testuser01' WHERE c.name='Innovation Cell'
ON CONFLICT(club_id,user_id) DO UPDATE SET mem_status='PENDING',role='MEMBER';

-- Eight linked event fixtures: upcoming, completed, cancelled, and pending review.
WITH seed(title, description, venue, category, club_name, day_offset, image_url, event_state, approval, registration_type) AS (
 VALUES
 ('Build for Campus Hackathon','A weekend team hackathon for useful campus tools.','Innovation Lab','Hackathon','Technobyte',14,'https://images.unsplash.com/photo-1504384308090-c894fdcc538d?auto=format&fit=crop&w=1200&q=80','UPCOMING','VERIFIED','THROUGH_APP'),
 ('Mobile Makers Workshop','Hands-on mobile app prototyping and API integration.','Engineering Block A','Workshop','Technobyte',21,'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80','UPCOMING','VERIFIED','THROUGH_APP'),
 ('Ideas to Impact','Student founders share practical lessons from early products.','Innovation Cell Hall','Seminar','Innovation Cell',28,'https://images.unsplash.com/photo-1556761175-b413da4baf72?auto=format&fit=crop&w=1200&q=80','UPCOMING','VERIFIED','THROUGH_APP'),
 ('Green Campus Design Sprint','Design small changes that make campus more sustainable.','Central Library','Competition','Innovation Cell',35,'https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=1200&q=80','UPCOMING','VERIFIED','THROUGH_APP'),
 ('Research Poster Walk','Explore student research posters and meet the presenters.','Jubilee Hall','Research & Innovation','Technobyte',-20,'https://images.unsplash.com/photo-1531058020387-3be344556be6?auto=format&fit=crop&w=1200&q=80','COMPLETED','COMPLETED','THROUGH_APP'),
 ('Campus Founder Fireside','An informal conversation with student founders.','Innovation Cell Hall','Entrepreneurship','Innovation Cell',42,'https://images.unsplash.com/photo-1511795409834-ef04bbd61622?auto=format&fit=crop&w=1200&q=80','UPCOMING','VERIFIED','THROUGH_APP'),
 ('Badminton Ladder Night','Friendly campus badminton ladder and social evening.','Sports Complex','Sports','Technobyte',-5,'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?auto=format&fit=crop&w=1200&q=80','CANCELLED','VERIFIED','THROUGH_APP'),
 ('AI Society Orientation','Meet the AI society and see current student projects.','Engineering Block B','Club Activity','Technobyte',50,'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?auto=format&fit=crop&w=1200&q=80','UPCOMING','PENDING','THROUGH_APP')
)
INSERT INTO events(title, description, creator_id, club_id, host_name, venue, latitude, longitude,
 start_time, end_time, visibility_type, registration_type, approval_status, event_state,
 base_priority_level, priority_score, priority_level)
SELECT s.title, s.description, u.id, c.id, c.name, s.venue, 29.9482, 76.8190,
 CURRENT_TIMESTAMP + (s.day_offset || ' days')::interval,
 CURRENT_TIMESTAMP + (s.day_offset || ' days')::interval + interval '4 hours',
 'PUBLIC',s.registration_type,s.approval,s.event_state,2,0,2
FROM seed s JOIN clubs c ON c.name=s.club_name
JOIN users u ON u.username='testuser01'
WHERE NOT EXISTS (SELECT 1 FROM events e WHERE e.title=s.title AND e.creator_id=u.id);

CREATE TEMP TABLE _test_events ON COMMIT DROP AS
SELECT e.id,e.title,CASE e.title
 WHEN 'Build for Campus Hackathon' THEN 1 WHEN 'Green Campus Design Sprint' THEN 2
 WHEN 'Ideas to Impact' THEN 3 WHEN 'Mobile Makers Workshop' THEN 4
 WHEN 'Research Poster Walk' THEN 5 WHEN 'Campus Founder Fireside' THEN 6
 WHEN 'Badminton Ladder Night' THEN 7 ELSE 8 END AS n
FROM events e WHERE e.title IN ('Build for Campus Hackathon','Mobile Makers Workshop','Ideas to Impact','Green Campus Design Sprint',
 'Research Poster Walk','Campus Founder Fireside','Badminton Ladder Night','AI Society Orientation')
  AND e.creator_id=(SELECT id FROM users WHERE username='testuser01');

INSERT INTO event_posters(event_id, poster_url)
SELECT e.id, s.image_url FROM _test_events e JOIN (
 VALUES ('Build for Campus Hackathon','https://images.unsplash.com/photo-1504384308090-c894fdcc538d?auto=format&fit=crop&w=1200&q=80'),
 ('Mobile Makers Workshop','https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80'),
 ('Ideas to Impact','https://images.unsplash.com/photo-1556761175-b413da4baf72?auto=format&fit=crop&w=1200&q=80'),
 ('Green Campus Design Sprint','https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=1200&q=80'),
 ('Research Poster Walk','https://images.unsplash.com/photo-1531058020387-3be344556be6?auto=format&fit=crop&w=1200&q=80'),
 ('Campus Founder Fireside','https://images.unsplash.com/photo-1511795409834-ef04bbd61622?auto=format&fit=crop&w=1200&q=80'),
 ('Badminton Ladder Night','https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?auto=format&fit=crop&w=1200&q=80'),
 ('AI Society Orientation','https://images.unsplash.com/photo-1485827404703-89b55fcc595e?auto=format&fit=crop&w=1200&q=80')
) s(title,image_url) ON s.title=e.title
WHERE NOT EXISTS (SELECT 1 FROM event_posters p WHERE p.event_id=e.id AND p.poster_url=s.image_url);

INSERT INTO event_category_map(event_id, category_id)
SELECT e.id, c.id FROM _test_events e JOIN event_categories c ON c.name=CASE e.n
 WHEN 1 THEN 'Hackathon' WHEN 2 THEN 'Competition' WHEN 3 THEN 'Seminar' WHEN 4 THEN 'Workshop'
 WHEN 5 THEN 'Research & Innovation' WHEN 6 THEN 'Entrepreneurship' WHEN 7 THEN 'Sports' ELSE 'Club Activity' END
ON CONFLICT(event_id, category_id) DO NOTHING;

INSERT INTO event_registration_config(event_id,min_team_members,max_team_members,quick_field_mask,
 registration_created_at,registration_end_at,auto_confirm)
SELECT id,CASE WHEN n IN (5,6) THEN 1 ELSE 2 END,4,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP + interval '10 days',TRUE FROM _test_events
ON CONFLICT(event_id) DO NOTHING;

INSERT INTO event_registration_fields(event_id,field_label,field_type,is_required,placeholder,field_order,individual)
SELECT e.id, f.label, f.field_type, TRUE, f.placeholder, f.ord, f.individual
FROM _test_events e CROSS JOIN (VALUES
 ('Preferred contribution','Choose a role', 'SELECT',1,TRUE),
 ('Relevant experience','Projects, skills, or interests','TEXT',2,TRUE),
 ('Team project idea','Describe your shared idea','TEXT',3,FALSE)
) f(label,placeholder,field_type,ord,individual)
WHERE NOT EXISTS (SELECT 1 FROM event_registration_fields x WHERE x.event_id=e.id AND x.field_label=f.label);

INSERT INTO event_registration_field_options(field_id,option_value,option_order)
SELECT f.id,o.value,o.ord FROM event_registration_fields f
JOIN _test_events e ON e.id=f.event_id
CROSS JOIN (VALUES ('Frontend',1),('Backend',2),('Design',3),('Research',4)) o(value,ord)
WHERE f.field_label='Preferred contribution'
AND NOT EXISTS (SELECT 1 FROM event_registration_field_options x WHERE x.field_id=f.id AND x.option_value=o.value);

-- Create one valid, complete three-person team for each event. Team leaders
-- always have matching registrations, satisfying V30's integrity checks.
INSERT INTO event_teams(event_id,team_name,leader_id)
SELECT e.id, CASE e.n WHEN 1 THEN 'Northstar' WHEN 2 THEN 'Green Sparks'
 WHEN 3 THEN 'Launch Crew' ELSE 'Pixel Pioneers' END,
 (SELECT id FROM users WHERE username=CASE e.n WHEN 1 THEN 'testuser01' WHEN 2 THEN 'testuser05'
 WHEN 3 THEN 'testuser08' ELSE 'testuser11' END)
FROM _test_events e
WHERE e.n<=4 AND NOT EXISTS (SELECT 1 FROM event_teams t WHERE t.event_id=e.id AND t.team_name=CASE e.n
 WHEN 1 THEN 'Northstar' WHEN 2 THEN 'Green Sparks' WHEN 3 THEN 'Launch Crew' ELSE 'Pixel Pioneers' END);

CREATE TEMP TABLE _test_team_registrants ON COMMIT DROP AS
SELECT e.id AS event_id, t.id AS team_id, u.id AS user_id
FROM _test_events e
JOIN event_teams t ON t.event_id=e.id AND t.team_name=CASE e.n
 WHEN 1 THEN 'Northstar' WHEN 2 THEN 'Green Sparks' WHEN 3 THEN 'Launch Crew' ELSE 'Pixel Pioneers' END
JOIN users u ON u.username IN (CASE e.n WHEN 1 THEN 'testuser01' WHEN 2 THEN 'testuser05' WHEN 3 THEN 'testuser08' ELSE 'testuser11' END,
 CASE e.n WHEN 1 THEN 'testuser02' WHEN 2 THEN 'testuser06' WHEN 3 THEN 'testuser09' ELSE 'testuser12' END,
 CASE e.n WHEN 1 THEN 'testuser03' WHEN 2 THEN 'testuser07' WHEN 3 THEN 'testuser10' ELSE 'testuser01' END)
WHERE e.n<=4;

INSERT INTO event_registrations(event_id,user_id,status,team_id,submitted_at)
SELECT event_id,user_id,CASE WHEN e.n=1 THEN 'PENDING' ELSE 'CONFIRMED' END,
 team_id,CURRENT_TIMESTAMP
FROM _test_team_registrants tr JOIN _test_events e ON e.id=tr.event_id
ON CONFLICT(event_id,user_id) DO NOTHING;

INSERT INTO event_members(event_id,user_id,role,team_id)
SELECT e.id,u.id,'CREATOR',NULL
FROM _test_events e JOIN users u ON u.username='testuser01'
ON CONFLICT(event_id,user_id) DO NOTHING;

INSERT INTO event_registration_answers(registration_id,field_id,answer)
SELECT r.id,f.id,CASE WHEN f.field_label='Preferred contribution' THEN
 CASE WHEN r.user_id % 4=0 THEN 'Frontend' WHEN r.user_id % 4=1 THEN 'Backend' WHEN r.user_id % 4=2 THEN 'Design' ELSE 'Research' END
 WHEN f.individual THEN 'I want to build and learn with a campus team.'
 ELSE 'A practical tool for students to discover campus opportunities.' END
FROM event_registrations r JOIN _test_events e ON e.id=r.event_id
JOIN event_registration_fields f ON f.event_id=e.id
WHERE r.team_id IS NOT NULL
ON CONFLICT(registration_id,field_id) DO NOTHING;

-- testuser01 also has a completed solo registration and one awaiting host review.
INSERT INTO event_registrations(event_id,user_id,status,team_id,submitted_at)
SELECT e.id,u.id,CASE WHEN e.n=5 THEN 'CONFIRMED' ELSE 'WAITLISTED' END,NULL,
 CASE WHEN e.n=5 THEN CURRENT_TIMESTAMP - interval '20 days' ELSE CURRENT_TIMESTAMP - interval '1 day' END
FROM _test_events e JOIN users u ON u.username='testuser01' WHERE e.n IN (5,6)
ON CONFLICT(event_id,user_id) DO NOTHING;
INSERT INTO event_registration_answers(registration_id,field_id,answer)
SELECT r.id,f.id,CASE WHEN f.field_label='Preferred contribution' THEN 'Research'
 ELSE 'I am interested in learning and contributing to the event.' END
FROM _test_events e JOIN event_registrations r ON r.event_id=e.id
JOIN users u ON u.id=r.user_id AND u.username='testuser01'
JOIN event_registration_fields f ON f.event_id=e.id AND f.individual=TRUE
WHERE e.n IN (5,6) AND r.team_id IS NULL
ON CONFLICT(registration_id,field_id) DO NOTHING;

-- One pending team join request for testing the leader review path.
INSERT INTO event_registrations(event_id,user_id,status,team_id,submitted_at)
SELECT e.id,u.id,'PENDING',NULL,NULL FROM _test_events e
JOIN users u ON u.username='testuser04' WHERE e.n=1
ON CONFLICT(event_id,user_id) DO UPDATE SET submitted_at=COALESCE(event_registrations.submitted_at,CURRENT_TIMESTAMP)
WHERE event_registrations.status='PENDING' AND event_registrations.team_id IS NULL;

INSERT INTO event_team_requests(team_id,registration_id,initiated_by,status)
SELECT t.id,r.id,'PARTICIPANT','PENDING'
FROM _test_events e
JOIN event_teams t ON t.event_id=e.id AND t.team_name='Northstar'
JOIN event_registrations r ON r.event_id=e.id AND r.user_id=(SELECT id FROM users WHERE username='testuser04')
WHERE e.n=1 AND r.status='PENDING' AND r.team_id IS NULL
AND NOT EXISTS (SELECT 1 FROM event_team_requests x WHERE x.registration_id=r.id AND x.status='PENDING');

INSERT INTO event_registration_answers(registration_id,field_id,answer)
SELECT r.id,f.id,CASE WHEN f.field_label='Preferred contribution' THEN 'Design'
 ELSE 'I can help with design feedback and usability testing.' END
FROM _test_events e JOIN event_registrations r ON r.event_id=e.id
JOIN users u ON u.id=r.user_id AND u.username='testuser04'
JOIN event_registration_fields f ON f.event_id=e.id AND f.individual=TRUE
WHERE e.n=1 AND r.status='PENDING' AND r.team_id IS NULL
ON CONFLICT(registration_id,field_id) DO NOTHING;

-- Community posts with remote image URLs, comments and votes.
INSERT INTO posts(creator_id,post_type,title,content_raw,content_rendered,visibility_type,allow_comments,created_at,updated_at)
SELECT u.id,'GENERAL',p.title,p.body,p.body,'PUBLIC',TRUE,CURRENT_TIMESTAMP - (p.n || ' days')::interval,CURRENT_TIMESTAMP - (p.n || ' days')::interval
FROM (VALUES
 ('testuser01','Hackathon team looking for a designer','We are prototyping a campus discovery app and have room for one more teammate.',1),
 ('testuser11','Workshop notes: small prototypes win','Our group learned to test the core interaction before polishing the UI.',2),
 ('testuser09','A greener campus starts with small changes','We are collecting ideas for the sustainability sprint next month.',3),
 ('testuser12','Project showcase this Friday','Bring a short demo and meet other builders from campus.',4),
 ('testuser01','UI testing session this week','I am hosting a short usability session for our campus discovery prototype. Join if you can test the flow.',5),
 ('testuser02','Sensor lab prototype','Our group has a working sensor demo and would love feedback on the enclosure.',6),
 ('testuser07','Robotics build log','The first chassis is ready. Next up: motor calibration and obstacle detection.',7),
 ('testuser06','Accessibility review volunteers','Looking for students to try a few screen reader and contrast checks.',8)
) p(username,title,body,n) JOIN users u ON u.username=p.username
WHERE NOT EXISTS (SELECT 1 FROM posts x WHERE x.creator_id=u.id AND x.title=p.title AND x.deleted_at IS NULL);

INSERT INTO post_images(post_id,image_url,image_order)
SELECT p.id, CASE p.title
 WHEN 'Hackathon team looking for a designer' THEN 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80'
 WHEN 'Workshop notes: small prototypes win' THEN 'https://images.unsplash.com/photo-1531482615713-2afd69097998?auto=format&fit=crop&w=1200&q=80'
 WHEN 'A greener campus starts with small changes' THEN 'https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=1200&q=80'
 ELSE 'https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=1200&q=80' END,1
FROM posts p WHERE p.title IN ('Hackathon team looking for a designer','Workshop notes: small prototypes win','A greener campus starts with small changes','Project showcase this Friday',
 'UI testing session this week','Sensor lab prototype','Robotics build log','Accessibility review volunteers')
AND EXISTS (SELECT 1 FROM users u WHERE u.id=p.creator_id AND u.username ~ '^testuser(0[1-9]|1[0-2])$')
AND NOT EXISTS (SELECT 1 FROM post_images i WHERE i.post_id=p.id);

INSERT INTO post_tags(name) VALUES ('campus'),('hackathon'),('workshop'),('sustainability')
ON CONFLICT(name) DO NOTHING;
INSERT INTO post_tag_map(post_id,tag_id)
SELECT p.id,t.id FROM posts p JOIN post_tags t ON t.name=CASE
 WHEN p.title ILIKE '%hackathon%' THEN 'hackathon'
 WHEN p.title ILIKE '%workshop%' THEN 'workshop'
 WHEN p.title ILIKE '%green%' THEN 'sustainability'
 ELSE 'campus' END
WHERE p.title IN ('Hackathon team looking for a designer','Workshop notes: small prototypes win','A greener campus starts with small changes','Project showcase this Friday',
 'UI testing session this week','Sensor lab prototype','Robotics build log','Accessibility review volunteers')
AND EXISTS (SELECT 1 FROM users u WHERE u.id=p.creator_id AND u.username ~ '^testuser(0[1-9]|1[0-2])$')
ON CONFLICT(post_id,tag_id) DO NOTHING;

INSERT INTO comments(post_id,creator_id,content)
SELECT p.id,u.id,'Count me in. I can help test the first version.'
FROM posts p JOIN users u ON u.username='testuser06'
WHERE p.title='Hackathon team looking for a designer'
AND EXISTS (SELECT 1 FROM users creator WHERE creator.id=p.creator_id AND creator.username ~ '^testuser(0[1-9]|1[0-2])$')
AND NOT EXISTS (SELECT 1 FROM comments c WHERE c.post_id=p.id AND c.creator_id=u.id);
INSERT INTO comments(post_id,creator_id,content)
SELECT p.id,u.id,'This is a great direction. I can share feedback from the first prototype.'
FROM posts p JOIN users u ON u.username='testuser01'
WHERE p.title='Workshop notes: small prototypes win'
AND EXISTS (SELECT 1 FROM users creator WHERE creator.id=p.creator_id AND creator.username='testuser11')
AND NOT EXISTS (SELECT 1 FROM comments c WHERE c.post_id=p.id AND c.creator_id=u.id);
INSERT INTO comments(post_id,creator_id,content)
SELECT p.id,u.id,'I can join the usability session and test the registration flow.'
FROM posts p JOIN users u ON u.username='testuser03'
WHERE p.title='UI testing session this week'
AND NOT EXISTS (SELECT 1 FROM comments c WHERE c.post_id=p.id AND c.creator_id=u.id);
INSERT INTO comments(post_id,creator_id,content)
SELECT p.id,u.id,'I can bring a device and help check accessibility.'
FROM posts p JOIN users u ON u.username='testuser01'
WHERE p.title='Accessibility review volunteers'
AND NOT EXISTS (SELECT 1 FROM comments c WHERE c.post_id=p.id AND c.creator_id=u.id);

INSERT INTO post_votes(post_id,user_id,vote_type)
SELECT p.id,u.id,'UPVOTE' FROM posts p CROSS JOIN users u
WHERE p.title IN ('Hackathon team looking for a designer','Workshop notes: small prototypes win',
 'UI testing session this week','Sensor lab prototype','Robotics build log','Accessibility review volunteers')
AND u.username IN ('testuser01','testuser02','testuser03','testuser08','testuser10','testuser12')
AND EXISTS (SELECT 1 FROM users creator WHERE creator.id=p.creator_id AND creator.username ~ '^testuser(0[1-9]|1[0-2])$')
ON CONFLICT(post_id,user_id) DO NOTHING;

-- Test honor definitions and score rows. Existing Flyway club/category/course
-- and POI seed data are intentionally untouched.
INSERT INTO honor_items(type,title,subtitle,icon_url,statistic_type,threshold)
SELECT 'BADGE',x.title,x.subtitle,x.icon,'PARTICIPATED_EVENTS',x.threshold
FROM (VALUES
 ('Campus Contributor','Participated in a campus event','https://images.unsplash.com/photo-1531058020387-3be344556be6?auto=format&fit=crop&w=256&q=80',1),
 ('Team Builder','Joined multiple event teams','https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=256&q=80',3)
) x(title,subtitle,icon,threshold)
WHERE NOT EXISTS (SELECT 1 FROM honor_items h WHERE h.title=x.title AND h.subtitle=x.subtitle);

INSERT INTO user_honor(user_id,honor_id,priority)
SELECT u.id,h.id,1 FROM users u JOIN honor_items h ON h.title='Campus Contributor'
WHERE u.username IN ('testuser01','testuser02','testuser05','testuser08','testuser11')
ON CONFLICT(user_id,honor_id) DO NOTHING;

INSERT INTO honor_points(user_id,points)
SELECT u.id,100 + (u.id % 9) * 25 FROM users u WHERE u.username ~ '^testuser(0[1-9]|1[0-2])$'
ON CONFLICT(user_id) DO NOTHING;

COMMIT;
