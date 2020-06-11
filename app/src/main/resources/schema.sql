CREATE TABLE IF NOT EXISTS lobby
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) UNIQUE,
    created_at TIMESTAMP,
    owner_id   UUID NOT NULL,
    version    INT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS lobby_membership
(
    lobby_id   UUID REFERENCES lobby (id),
    player_id  UUID NOT NULL,
    created_at TIMESTAMP,
    PRIMARY KEY (lobby_id, player_id)
)