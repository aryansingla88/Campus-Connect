USE campus_connect;
select * from users;

CREATE TABLE users_new LIKE users;

ALTER TABLE users_new MODIFY id INT AUTO_INCREMENT;

INSERT INTO users_new (username, email, password_hash)
SELECT username, email, password_hash FROM users;

DROP TABLE users;

RENAME TABLE users_new TO users;

-- USERS
INSERT INTO users (username, email, password_hash) VALUES
('student1', 'student1@campus.edu', 'hash1'),
('student2', 'student2@campus.edu', 'hash2'),
('student3', 'student3@campus.edu', 'hash3'),
('student4', 'student4@campus.edu', 'hash4'),
('student5', 'student5@campus.edu', 'hash5'),
('student6', 'student6@campus.edu', 'hash6'),
('student7', 'student7@campus.edu', 'hash7'),
('student8', 'student8@campus.edu', 'hash8');

-- USER PRESENCE
INSERT INTO user_presence (user_id, latitude, longitude, last_update) VALUES
(1, 31.2505, 75.7050, NOW()),
(2, 31.2507, 75.7052, NOW()),
(3, 31.2509, 75.7054, NOW()),
(4, 31.2511, 75.7056, NOW()),
(5, 31.2513, 75.7058, NOW()),
(6, 31.2515, 75.7060, NOW()),
(7, 31.2517, 75.7062, NOW()),
(8, 31.2519, 75.7064, NOW());

-- POSTS
INSERT INTO posts (user_id, content) VALUES
(1, 'Anyone studying in the library today?'),
(2, 'Badminton match today evening at sports complex!'),
(3, 'Cafeteria food is amazing today'),
(4, 'Looking for project partners for DBMS'),
(5, 'Anyone going to the swimming pool?'),
(6, 'Tech talk happening in senate hall tomorrow'),
(7, 'Group study at lecture hall tonight'),
(8, 'Campus looks beautiful after rain');

-- COMMENTS
INSERT INTO comments (post_id, user_id, content) VALUES
(1, 2, 'Yes I am at the library'),
(1, 3, 'Coming in 10 minutes'),
(2, 4, 'Count me in'),
(3, 5, 'What is the special today?'),
(4, 6, 'I can join the project'),
(5, 7, 'I will come too'),
(6, 8, 'Sounds interesting'),
(7, 1, 'See you there');

-- UPVOTES
INSERT INTO upvote (user_id, post_id) VALUES
(2,1),
(3,1),
(4,1),
(1,2),
(5,2),
(6,3),
(7,4),
(8,5),
(3,6),
(2,7),
(4,8);

-- EVENTS
INSERT INTO events (title, description, poi_id, event_time, created_by) VALUES
('AI Seminar', 'Artificial Intelligence seminar by MCA department', 25, '2025-04-10 14:00:00', 1),
('Badminton Tournament', 'Friendly badminton competition', 14, '2025-04-11 17:00:00', 2),
('Coding Workshop', 'Hands-on coding session', 24, '2025-04-12 11:00:00', 3),
('Swimming Competition', 'Campus swimming event', 13, '2025-04-13 16:00:00', 4);

-- EVENT PARTICIPANTS
INSERT INTO event_participants (user_id, event_id) VALUES
(2,1),
(3,1),
(4,1),
(1,2),
(5,2),
(6,2),
(3,3),
(7,3),
(8,3),
(2,4),
(4,4),
(6,4);

-- FRIENDSHIPS
INSERT INTO friendships (user_id, friend_id, status) VALUES
(1,2,'accepted'),
(1,3,'accepted'),
(2,3,'accepted'),
(2,4,'pending'),
(3,5,'accepted'),
(4,6,'accepted'),
(5,7,'pending'),
(6,8,'accepted');

-- NOTIFICATIONS
INSERT INTO notifications (user_id, type, message, is_read) VALUES
(1,'like','Your post received a new upvote',FALSE),
(2,'comment','Someone commented on your post',FALSE),
(3,'event','New event announced on campus',FALSE),
(4,'friend','You received a friend request',FALSE),
(5,'like','Your post was liked',FALSE),
(6,'event','Coding workshop announced',FALSE),
(7,'comment','Someone replied to your comment',FALSE),
(8,'friend','Your friend request was accepted',FALSE);