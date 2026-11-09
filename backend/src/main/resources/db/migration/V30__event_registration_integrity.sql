-- Refuse to normalize or delete user data implicitly. These diagnostics identify
-- rows that must be reviewed before the new uniqueness rule can be installed.
DO $$
DECLARE duplicate_names TEXT;
DECLARE duplicate_leaders TEXT;
DECLARE invalid_links TEXT;
BEGIN
    SELECT string_agg(format('event=%s name=%L teams=%s', event_id, normalized_name, team_ids), '; ')
      INTO duplicate_names
      FROM (
          SELECT event_id,
                 lower(btrim(team_name)) AS normalized_name,
                 string_agg(id::text, ',') AS team_ids
            FROM event_teams
           GROUP BY event_id, lower(btrim(team_name))
          HAVING count(*) > 1
      ) duplicates;
    IF duplicate_names IS NOT NULL THEN
        RAISE EXCEPTION 'Duplicate event team names must be reviewed before migration V30: %', duplicate_names;
    END IF;

    SELECT string_agg(format('event=%s leader=%s teams=%s', event_id, leader_id, team_ids), '; ')
      INTO duplicate_leaders
      FROM (
          SELECT event_id, leader_id, string_agg(id::text, ',') AS team_ids
            FROM event_teams
           GROUP BY event_id, leader_id
          HAVING count(*) > 1
      ) duplicates;
    IF duplicate_leaders IS NOT NULL THEN
        RAISE EXCEPTION 'Duplicate active team leaders must be reviewed before migration V30: %', duplicate_leaders;
    END IF;

    SELECT string_agg(format('team=%s event=%s leader=%s', t.id, t.event_id, t.leader_id), '; ')
      INTO invalid_links
      FROM event_teams t
      LEFT JOIN event_registrations r
        ON r.event_id = t.event_id
       AND r.user_id = t.leader_id
       AND r.team_id = t.id
     WHERE r.id IS NULL;
    IF invalid_links IS NOT NULL THEN
        RAISE EXCEPTION 'Teams without matching leader registrations must be reviewed before migration V30: %', invalid_links;
    END IF;
END $$;

CREATE UNIQUE INDEX uq_event_teams_event_normalized_name
    ON event_teams(event_id, lower(btrim(team_name)));

CREATE UNIQUE INDEX uq_event_teams_event_leader
    ON event_teams(event_id, leader_id);

-- Drafts are not submissions. Keep the existing timestamp for submitted rows,
-- but clear it for drafts and allow it to be set when finalization succeeds.
ALTER TABLE event_registrations
    ALTER COLUMN submitted_at DROP NOT NULL;

UPDATE event_registrations
   SET submitted_at = NULL
 WHERE status = 'PENDING';






