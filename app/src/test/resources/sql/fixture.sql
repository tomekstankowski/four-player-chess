INSERT INTO lobby(id, name, created_at, owner_id)
VALUES ('78004565-85e8-4258-bd7e-cebe59571284', 'Pokój #1', '2020-01-31T22:08:12',
        'df1a63de-c6b9-4383-ab56-761e3339be6c'),
       ('2025c4cf-3e70-4730-8474-21cc6a760647', 'Pokój #2', '2020-01-31T22:08:33',
        'b4467e4d-e9cd-416c-93c6-29d60f682ba8');

INSERT INTO lobby_membership(lobby_id, player_id, created_at)
VALUES ('78004565-85e8-4258-bd7e-cebe59571284', 'df1a63de-c6b9-4383-ab56-761e3339be6c', '2020-01-31T22:08:12'),
       ('78004565-85e8-4258-bd7e-cebe59571284', 'ff698d5e-d1a7-45d4-9e12-18d88bdf517e', '2020-01-31T22:08:15'),

       ('2025c4cf-3e70-4730-8474-21cc6a760647', 'b4467e4d-e9cd-416c-93c6-29d60f682ba8', '2020-01-31T22:08:33');