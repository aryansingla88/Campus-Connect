
ALTER TABLE honor_items
DROP COLUMN condition;

ALTER TABLE honor_items
    ADD COLUMN statistic_type VARCHAR(50);

ALTER TABLE honor_items
    ADD COLUMN threshold INTEGER;