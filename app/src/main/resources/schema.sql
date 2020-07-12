CREATE TABLE IF NOT EXISTS game
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP,
    committed  BOOL,
    cancelled  BOOL,
    finished   BOOL
);

CREATE TABLE IF NOT EXISTS human_game_player
(
    game_id UUID NOT NULL REFERENCES game (id),
    user_id UUID NOT NULL,
    color   VARCHAR(255),
    PRIMARY KEY (game_id, color)
);

CREATE TABLE IF NOT EXISTS random_bot_game_player
(
    game_id UUID NOT NULL REFERENCES game (id),
    color   VARCHAR(255),
    PRIMARY KEY (game_id, color)
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

CREATE TABLE IF NOT EXISTS human_player_lobby_membership
(
    lobby_id   UUID NOT NULL REFERENCES lobby (id),
    user_id    UUID NOT NULL,
    created_at TIMESTAMP,
    PRIMARY KEY (lobby_id, user_id)
);

CREATE TABLE IF NOT EXISTS random_bot_lobby_membership
(
    bot_id     UUID NOT NULL,
    lobby_id   UUID NOT NULL REFERENCES lobby (id),
    created_at TIMESTAMP,
    PRIMARY KEY (lobby_id, bot_id)
);