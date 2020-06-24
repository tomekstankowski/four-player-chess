INSERT INTO lobby(id, name, created_at, owner_id, deleted, game_id)
VALUES ('78004565-85e8-4258-bd7e-cebe59571284', 'Pokój #1', '2020-01-31T22:08:12',
        'df1a63de-c6b9-4383-ab56-761e3339be6c', FALSE, NULL),
       ('2025c4cf-3e70-4730-8474-21cc6a760647', 'Pokój #2', '2020-01-31T22:08:33',
        'b4467e4d-e9cd-416c-93c6-29d60f682ba8', FALSE, NULL),
       ('9697072c-979d-4c60-ba08-b0fa637332bb', 'Pokój #3', '2020-01-31T22:08:36',
        '440d1b8b-12aa-4b80-9190-627d4d99c317', TRUE, NULL),
       ('c84ca410-d97a-4ef5-8fb3-e59309848c96', 'Pokój #4', '2020-01-31T22:08:38',
        '7a2c4088-045e-4513-97f0-7900a7231305', FALSE, NULL);

INSERT INTO lobby_membership(lobby_id, player_id, created_at)
VALUES ('78004565-85e8-4258-bd7e-cebe59571284', 'df1a63de-c6b9-4383-ab56-761e3339be6c', '2020-01-31T22:08:12'),
       ('78004565-85e8-4258-bd7e-cebe59571284', 'ff698d5e-d1a7-45d4-9e12-18d88bdf517e', '2020-01-31T22:08:15'),

       ('2025c4cf-3e70-4730-8474-21cc6a760647', 'b4467e4d-e9cd-416c-93c6-29d60f682ba8', '2020-01-31T22:08:33'),

       ('9697072c-979d-4c60-ba08-b0fa637332bb', '440d1b8b-12aa-4b80-9190-627d4d99c317', '2020-01-31T22:08:36'),

       ('c84ca410-d97a-4ef5-8fb3-e59309848c96', '7a2c4088-045e-4513-97f0-7900a7231305', '2020-01-31T22:08:38'),
       ('c84ca410-d97a-4ef5-8fb3-e59309848c96', 'f884283a-7ab6-4f1f-8491-d94e8897898e', '2020-01-31T22:08:39'),
       ('c84ca410-d97a-4ef5-8fb3-e59309848c96', '23a79093-ce9f-4387-a4e1-b10420f2e770', '2020-01-31T22:08:39'),
       ('c84ca410-d97a-4ef5-8fb3-e59309848c96', '18dff866-6aa3-43ac-9385-0547f68bd662', '2020-01-31T22:08:39');

INSERT INTO game (id, created_at, committed, cancelled)
VALUES ('3cc49522-81f2-44d1-a73c-8ba35dddc219', '2020-06-24T21:13:02', TRUE, FALSE);

INSERT INTO game_player(game_id, player_id, color)
VALUES ('3cc49522-81f2-44d1-a73c-8ba35dddc219', '02d37d81-d1e1-4719-872a-ccab471ea908', 'Red'),
       ('3cc49522-81f2-44d1-a73c-8ba35dddc219', '4f56f39c-2f52-4015-9353-49c7580458b3', 'Blue'),
       ('3cc49522-81f2-44d1-a73c-8ba35dddc219', '2f0bc4e1-5b36-47f7-a0b3-49bf9109696d', 'Yellow'),
       ('3cc49522-81f2-44d1-a73c-8ba35dddc219', '7db8e058-6101-4a34-a92d-871f6444acac', 'Green');