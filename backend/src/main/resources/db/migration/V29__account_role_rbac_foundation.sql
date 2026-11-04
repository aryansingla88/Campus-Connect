-- Account roles remain VARCHAR values. Keep legacy ADMIN and MODERATOR values
-- intact until an operator decides how each existing account should be handled.
-- They are not granted Spring Security authorities by the RBAC foundation.
ALTER TABLE users
    ALTER COLUMN role SET DEFAULT 'STUDENT';

UPDATE users
SET role = 'STUDENT'
WHERE role IS NULL OR BTRIM(role) = '';

COMMENT ON COLUMN users.role IS
    'Account role: STUDENT, CLUB_ADMIN, TEACHER, DEV, or CAMPUS_CONNECT. Legacy ADMIN/MODERATOR values are retained pending manual review.';
