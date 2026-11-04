CREATE TABLE history_items (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    type         VARCHAR(10)  NOT NULL CHECK (type IN ('TEXT', 'FILE')),
    claim_code   VARCHAR(10)  NOT NULL,
    text_content TEXT,
    object_key   VARCHAR(255),
    file_name    VARCHAR(255),
    content_type VARCHAR(255),
    size         BIGINT,
    created_at   TIMESTAMPTZ  NOT NULL,
    expires_at   TIMESTAMPTZ  NOT NULL,
    CHECK ((type = 'TEXT' AND text_content IS NOT NULL)
        OR (type = 'FILE' AND object_key IS NOT NULL))
);

CREATE INDEX idx_history_items_user_created ON history_items (user_id, created_at DESC);
