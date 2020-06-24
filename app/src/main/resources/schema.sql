CREATE TABLE IF NOT EXISTS game
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP,
    committed  BOOL,
    cancelled  BOOL
);

CREATE TABLE IF NOT EXISTS game_player
(
    game_id   UUID NOT NULL REFERENCES game (id),
    player_id UUID NOT NULL,
    color     VARCHAR(255),
    PRIMARY KEY (game_id, player_id)
);

CREATE TABLE IF NOT EXISTS lobby
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(255),
    created_at TIMESTAMP,
    owner_id   UUID NOT NULL,
    game_id    UUID REFERENCES game (id),
    deleted    BOOL,
    version    INT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS lobby_membership
(
    lobby_id   UUID NOT NULL REFERENCES lobby (id),
    player_id  UUID NOT NULL,
    created_at TIMESTAMP,
    PRIMARY KEY (lobby_id, player_id)
);