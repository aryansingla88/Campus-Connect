-- Enable PostGIS Extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Navigation Nodes Table
CREATE TABLE IF NOT EXISTS nav_nodes (
                                         id BIGSERIAL PRIMARY KEY,
                                         name VARCHAR(255),
    location GEOGRAPHY(POINT, 4326) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_nav_nodes_location ON nav_nodes USING GIST(location);

-- Navigation Edges Table
CREATE TABLE IF NOT EXISTS nav_edges (
                                         id BIGSERIAL PRIMARY KEY,
                                         from_node_id BIGINT NOT NULL REFERENCES nav_nodes(id) ON DELETE CASCADE,
    to_node_id BIGINT NOT NULL REFERENCES nav_nodes(id) ON DELETE CASCADE,
    distance_meters DOUBLE PRECISION NOT NULL,
    is_two_way BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Destination Nav Mapping Table
CREATE TABLE IF NOT EXISTS destination_nav_mappings (
                                                        id BIGSERIAL PRIMARY KEY,
                                                        entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    nav_node_id BIGINT NOT NULL REFERENCES nav_nodes(id) ON DELETE CASCADE,
    CONSTRAINT unique_entity_nav UNIQUE (entity_type, entity_id)
    );