CREATE TABLE IF NOT EXISTS examples(
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    state TEXT NOT NULL DEFAULT 'ACTIVE',
    version INTEGER NOT NULL DEFAULT 0,
    deleted BOOLEAN,
    created TIMESTAMP WITH TIME ZONE,
    created_by TEXT,
    modified TIMESTAMP WITH TIME ZONE,
    modified_by TEXT
);
CREATE TABLE IF NOT EXISTS example_entity_tags(
    example_entity_id UUID,
    key TEXT NOT NULL,
    value TEXT,
    PRIMARY KEY (example_entity_id, key),
    FOREIGN KEY(example_entity_id) REFERENCES examples(id)
);
