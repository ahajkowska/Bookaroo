-- Użytkownicy
INSERT INTO users (id, username, email, password, role, avatar, bio, is_locked, created_at)
VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'magda_gessler', 'magda@bookaroo.com', '$2a$12$eZ80rN1TUJkTB.b31pLoB.zGPeu1U47de1p.tq8RwHEvt/j/2Tshm', 'ADMIN', '/uploads/admin_avatar.png', 'Kreatorka smaku i dobrej literatury.', false, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440003', 'jankowalski', 'jankowalski@bookaroo.com', '$2a$12$eZ80rN1TUJkTB.b31pLoB.zGPeu1U47de1p.tq8RwHEvt/j/2Tshm', 'ADMIN', '/uploads/avatar3.png', 'Dzień dobry. Miłego czytania.', false, CURRENT_TIMESTAMP),

    ('550e8400-e29b-41d4-a716-446655440000', 'adam_malysz', 'adam@bookaroo.com', '$2a$12$eZ80rN1TUJkTB.b31pLoB.zGPeu1U47de1p.tq8RwHEvt/j/2Tshm', 'USER', '/uploads/avatar1.png', 'Lubię skakać po stronach dobrych książek.', false, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440002', 'robert_kubica', 'robert@bookaroo.com', '$2a$12$eZ80rN1TUJkTB.b31pLoB.zGPeu1U47de1p.tq8RwHEvt/j/2Tshm', 'USER', '/uploads/avatar2.png', 'Szybkie czytanie to moja pasja.', false, CURRENT_TIMESTAMP);

-- Gatunki
INSERT INTO genres (id, name, description)
VALUES
    ('660e8400-e29b-41d4-a716-446655440000', 'Fantasy', 'Magia, smoki i epickie przygody'),
    ('660e8400-e29b-41d4-a716-446655440001', 'Kryminał', 'Zbrodnia, śledztwo i tajemnica'),
    ('660e8400-e29b-41d4-a716-446655440002', 'Romans', 'Miłość w roli głównej'),
    ('660e8400-e29b-41d4-a716-446655440003', 'Science Fiction', 'Przyszłość, kosmos i technologia'),
    ('660e8400-e29b-41d4-a716-446655440004', 'Literatura klasyczna', 'Ponadczasowe dzieła'),
    ('660e8400-e29b-41d4-a716-446655440005', 'Horror', 'Książki, które nie pozwolą Ci zasnąć'),
    ('660e8400-e29b-41d4-a716-446655440006', 'Reportaż', 'Literatura faktu');

-- Autorzy
INSERT INTO authors (id, name, surname) VALUES
                                            ('11111111-1111-1111-1111-111111111111', 'J.R.R.', 'Tolkien'),
                                            ('22222222-2222-2222-2222-222222222222', 'George', 'Orwell'),
                                            ('33333333-3333-3333-3333-333333333333', 'Andrzej', 'Sapkowski'),
                                            ('44444444-4444-4444-4444-444444444444', 'Stephen', 'King'),
                                            ('55555555-5555-5555-5555-555555555555', 'Frank', 'Herbert'),
                                            ('66666666-6666-6666-6666-666666666666', 'J.K.', 'Rowling'),
                                            ('77777777-7777-7777-7777-777777777777', 'Agatha', 'Christie');

-- Książki
INSERT INTO books (id, title, isbn, description, publication_year, author_id) VALUES
                                                                                  -- Tolkien
                                                                                  ('33333333-3333-3333-3333-333333333333', 'Władca Pierścieni: Drużyna Pierścienia', '978-1234567890', 'Początek wielkiej wyprawy Froda.', 1954, '11111111-1111-1111-1111-111111111111'),
                                                                                  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Hobbit', '978-0007102021', 'Przygody Bilbo Bagginsa.', 1937, '11111111-1111-1111-1111-111111111111'),

                                                                                  -- Orwell
                                                                                  ('44444444-4444-4444-4444-444444444444', 'Rok 1984', '978-0987654321', 'Wielki Brat patrzy.', 1949, '22222222-2222-2222-2222-222222222222'),
                                                                                  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Folwark Zwierzęcy', '978-0141036137', 'Wszystkie zwierzęta są równe, ale...', 1945, '22222222-2222-2222-2222-222222222222'),

                                                                                  -- Sapkowski
                                                                                  ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Ostatnie Życzenie', '978-8375780635', 'Geralt z Rivii wkracza do akcji.', 1993, '33333333-3333-3333-3333-333333333333'),

                                                                                  -- Stephen King
                                                                                  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Lśnienie', '978-0307743657', 'Hotel Overlook czeka na gości.', 1977, '44444444-4444-4444-4444-444444444444'),
                                                                                  ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'Zielona Mila', '978-0671041786', 'Cuda w celi śmierci.', 1996, '44444444-4444-4444-4444-444444444444'),

                                                                                  -- Frank Herbert
                                                                                  ('00000000-0000-0000-0000-000000000001', 'Diuna', '978-0441172719', 'Arrakis. Diuna. Pustynna Planeta.', 1965, '55555555-5555-5555-5555-555555555555'),

                                                                                  -- J.K. Rowling
                                                                                  ('00000000-0000-0000-0000-000000000002', 'Harry Potter i Kamień Filozoficzny', '978-0747532743', 'Chłopiec, który przeżył.', 1997, '66666666-6666-6666-6666-666666666666'),

                                                                                  -- Agatha Christie
                                                                                  ('00000000-0000-0000-0000-000000000003', 'Morderstwo w Orient Expressie', '978-0062073501', 'Herkules Poirot rozwiązuje zagadkę.', 1934, '77777777-7777-7777-7777-777777777777');

-- Książka <-> Gatunek
INSERT INTO book_genres (book_id, genre_id) VALUES
                                                -- Władca Pierścieni -> Fantasy, Klasyka
                                                ('33333333-3333-3333-3333-333333333333', '660e8400-e29b-41d4-a716-446655440000'),
                                                ('33333333-3333-3333-3333-333333333333', '660e8400-e29b-41d4-a716-446655440004'),

                                                -- Hobbit -> Fantasy, Klasyka
                                                ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '660e8400-e29b-41d4-a716-446655440000'),
                                                ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '660e8400-e29b-41d4-a716-446655440004'),

                                                -- Rok 1984 -> Sci-Fi, Klasyka
                                                ('44444444-4444-4444-4444-444444444444', '660e8400-e29b-41d4-a716-446655440003'),
                                                ('44444444-4444-4444-4444-444444444444', '660e8400-e29b-41d4-a716-446655440004'),

                                                -- Folwark Zwierzęcy -> Klasyka
                                                ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '660e8400-e29b-41d4-a716-446655440004'),

                                                -- Ostatnie Życzenie -> Fantasy
                                                ('cccccccc-cccc-cccc-cccc-cccccccccccc', '660e8400-e29b-41d4-a716-446655440000'),

                                                -- Lśnienie -> Horror, Kryminał (Thriller)
                                                ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '660e8400-e29b-41d4-a716-446655440005'),
                                                ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '660e8400-e29b-41d4-a716-446655440001'),

                                                -- Zielona Mila -> Kryminał, Fantasy, Klasyka
                                                ('ffffffff-ffff-ffff-ffff-ffffffffffff', '660e8400-e29b-41d4-a716-446655440001'),
                                                ('ffffffff-ffff-ffff-ffff-ffffffffffff', '660e8400-e29b-41d4-a716-446655440000'),
                                                ('ffffffff-ffff-ffff-ffff-ffffffffffff', '660e8400-e29b-41d4-a716-446655440004'),

                                                -- Diuna -> Sci-Fi
                                                ('00000000-0000-0000-0000-000000000001', '660e8400-e29b-41d4-a716-446655440003'),

                                                -- Harry Potter -> Fantasy
                                                ('00000000-0000-0000-0000-000000000002', '660e8400-e29b-41d4-a716-446655440000'),

                                                -- Orient Express -> Kryminał, Klasyka
                                                ('00000000-0000-0000-0000-000000000003', '660e8400-e29b-41d4-a716-446655440001'),
                                                ('00000000-0000-0000-0000-000000000003', '660e8400-e29b-41d4-a716-446655440004');

-- Recenzje
INSERT INTO reviews (id, rating, content, created_at, book_id, user_id) VALUES
                                                                            -- dla "Władcy Pierścieni"
                                                                            -- Magda Gessler ocenia na 10/10
                                                                            ('990e8400-e29b-41d4-a716-446655440001', 10, 'Absolutne arcydzieło! Lepsze niż moje torty.', CURRENT_TIMESTAMP, '33333333-3333-3333-3333-333333333333', '550e8400-e29b-41d4-a716-446655440001'),

                                                                            -- Adam Małysz -> 9/10
                                                                            ('990e8400-e29b-41d4-a716-446655440002', 9, 'Długa podróż, ale warto. Trochę jak sezon skoków.', CURRENT_TIMESTAMP, '33333333-3333-3333-3333-333333333333', '550e8400-e29b-41d4-a716-446655440000'),

                                                                            -- dla "Rok 1984"
                                                                            -- Robert Kubica -> 8/10
                                                                            ('990e8400-e29b-41d4-a716-446655440003', 8, 'Mocna i przygnębiająca. Szybko się czyta.', CURRENT_TIMESTAMP, '44444444-4444-4444-4444-444444444444', '550e8400-e29b-41d4-a716-446655440002'),

                                                                            -- Adam Małysz -> 5/10
                                                                            ('990e8400-e29b-41d4-a716-446655440004', 5, 'Dla mnie zbyt mroczna wizja.', CURRENT_TIMESTAMP, '44444444-4444-4444-4444-444444444444', '550e8400-e29b-41d4-a716-446655440000'),

                                                                            -- dla "Wiedźmina"
                                                                            -- Magda Gessler -> 10/10
                                                                            ('990e8400-e29b-41d4-a716-446655440005', 10, 'Geralt ma charakter! Polecam każdemu.', CURRENT_TIMESTAMP, 'cccccccc-cccc-cccc-cccc-cccccccccccc', '550e8400-e29b-41d4-a716-446655440001'),

                                                                            -- Robert Kubica o->  7/10
                                                                            ('990e8400-e29b-41d4-a716-446655440006', 7, 'Dobre walki, ale polityka nudna.', CURRENT_TIMESTAMP, 'cccccccc-cccc-cccc-cccc-cccccccccccc', '550e8400-e29b-41d4-a716-446655440002');

-- UPDATE users SET role = 'ADMIN' WHERE username = 'jankowalski';
-- mvn clean test jacoco:report