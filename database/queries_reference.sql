-- =====================================================
-- USERS
-- =====================================================

-- Register User
INSERT INTO users (username, email, password_hash)
VALUES (?, ?, ?);

-- Login User
SELECT * FROM users
WHERE email = ?;

-- Get User By ID
SELECT * FROM users
WHERE id = ?;

-- Update Username
UPDATE users
SET username = ?
WHERE id = ?;

-- Delete User
DELETE FROM users
WHERE id = ?;



-- =====================================================
-- USER PRESENCE (LIVE LOCATION)
-- =====================================================

-- Insert or Update Location
INSERT INTO user_presence (user_id, latitude, longitude, last_update)
VALUES (?, ?, ?, NOW())
ON DUPLICATE KEY UPDATE
latitude = VALUES(latitude),
longitude = VALUES(longitude),
last_update = NOW();

-- Get All Active Users On Map
SELECT * FROM user_presence;

-- Get Specific User Location
SELECT * FROM user_presence
WHERE user_id = ?;

-- Remove Presence Record
DELETE FROM user_presence
WHERE user_id = ?;



-- =====================================================
-- POSTS
-- =====================================================

-- Create Post
INSERT INTO posts (user_id, content)
VALUES (?, ?);

-- Get All Posts (Feed)
SELECT p.id, p.content, p.created_at, u.username
FROM posts p
JOIN users u ON p.user_id = u.id
ORDER BY p.created_at DESC;

-- Get Posts By User
SELECT * FROM posts
WHERE user_id = ?
ORDER BY created_at DESC;

-- Update Post
UPDATE posts
SET content = ?
WHERE id = ?;

-- Delete Post
DELETE FROM posts
WHERE id = ?;



-- =====================================================
-- COMMENTS
-- =====================================================

-- Add Comment
INSERT INTO comments (post_id, user_id, content)
VALUES (?, ?, ?);

-- Get Comments For Post
SELECT c.id, c.content, c.created_at, u.username
FROM comments c
JOIN users u ON c.user_id = u.id
WHERE c.post_id = ?
ORDER BY c.created_at;

-- Update Comment
UPDATE comments
SET content = ?
WHERE id = ?;

-- Delete Comment
DELETE FROM comments
WHERE id = ?;



-- =====================================================
-- UPVOTES
-- =====================================================

-- Add Upvote
INSERT INTO upvote (user_id, post_id)
VALUES (?, ?);

-- Remove Upvote
DELETE FROM upvote
WHERE user_id = ? AND post_id = ?;

-- Count Upvotes On Post
SELECT COUNT(*) AS upvote_count
FROM upvote
WHERE post_id = ?;

-- Check If User Already Upvoted
SELECT * FROM upvote
WHERE user_id = ? AND post_id = ?;



-- =====================================================
-- EVENTS
-- =====================================================

-- Create Event
INSERT INTO events (title, description, poi_id, event_time, created_by)
VALUES (?, ?, ?, ?, ?);

-- Get All Events
SELECT e.id, e.title, e.description, e.event_time, p.name AS location
FROM events e
JOIN poi p ON e.poi_id = p.id
ORDER BY e.event_time;

-- Get Event By ID
SELECT * FROM events
WHERE id = ?;

-- Get Events Created By User
SELECT * FROM events
WHERE created_by = ?;

-- Update Event
UPDATE events
SET title = ?, description = ?, event_time = ?
WHERE id = ?;

-- Delete Event
DELETE FROM events
WHERE id = ?;



-- =====================================================
-- EVENT PARTICIPANTS
-- =====================================================

-- Join Event
INSERT INTO event_participants (user_id, event_id)
VALUES (?, ?);

-- Leave Event
DELETE FROM event_participants
WHERE user_id = ? AND event_id = ?;

-- Get Participants Of Event
SELECT ep.user_id, u.username
FROM event_participants ep
JOIN users u ON ep.user_id = u.id
WHERE ep.event_id = ?;

-- Count Event Participants
SELECT COUNT(*) AS participant_count
FROM event_participants
WHERE event_id = ?;



-- =====================================================
-- FRIENDSHIPS
-- =====================================================

-- Send Friend Request
INSERT INTO friendships (user_id, friend_id, status)
VALUES (?, ?, 'pending');

-- Accept Friend Request
UPDATE friendships
SET status = 'accepted'
WHERE user_id = ? AND friend_id = ?;

-- Cancel / Remove Friendship
DELETE FROM friendships
WHERE user_id = ? AND friend_id = ?;

-- Get All Friends Of User
SELECT *
FROM friendships
WHERE user_id = ? OR friend_id = ?;

-- Get Pending Friend Requests
SELECT *
FROM friendships
WHERE friend_id = ? AND status = 'pending';



-- =====================================================
-- POI (CAMPUS LOCATIONS)
-- =====================================================

-- Get All Campus Locations
SELECT * FROM poi;

-- Get POI By Category
SELECT * FROM poi
WHERE category = ?;

-- Search Location By Name
SELECT * FROM poi
WHERE name LIKE CONCAT('%', ?, '%');



-- =====================================================
-- NOTIFICATIONS
-- =====================================================

-- Create Notification
INSERT INTO notifications (user_id, type, message, is_read)
VALUES (?, ?, ?, FALSE);

-- Get Notifications For User
SELECT * FROM notifications
WHERE user_id = ?
ORDER BY created_at DESC;

-- Mark Notification As Read
UPDATE notifications
SET is_read = TRUE
WHERE id = ?;

-- Delete Notification
DELETE FROM notifications
WHERE id = ?;



-- =====================================================
-- ANALYTICS / INSIGHTS
-- =====================================================

-- Most Active Users (by posts)
SELECT user_id, COUNT(*) AS total_posts
FROM posts
GROUP BY user_id
ORDER BY total_posts DESC;

-- Most Popular Posts
SELECT post_id, COUNT(*) AS upvotes
FROM upvote
GROUP BY post_id
ORDER BY upvotes DESC;

-- Upcoming Events
SELECT * FROM events
WHERE event_time > NOW()
ORDER BY event_time;

-- Most Active Event (highest participants)
SELECT event_id, COUNT(*) AS participants
FROM event_participants
GROUP BY event_id
ORDER BY participants DESC;
