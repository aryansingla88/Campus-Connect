UPDATE club_members
SET role = 'COMMITTEE_HEAD'
WHERE upper(role) = 'ADMIN';
