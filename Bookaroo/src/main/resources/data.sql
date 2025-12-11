-- Przykładowi użytkownicy (czytelnicy)
INSERT INTO users (id, username, email, password, role, avatar, bio, created_at)
VALUES
    ('550e8400-e29b-41d4-a716-446655440000', 'adam_malysz', 'adam@bookaroo.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 'https://i.pravatar.cc/150?img=12', 'Uwielbiam czytać fantasy i kryminały!', CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440001', 'magda_gessler', 'magda@bookaroo.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'https://i.pravatar.cc/150?img=25', 'Administrator społeczności Book Lovers. Pasjonatka literatury pięknej.', CURRENT_TIMESTAMP);

-- Przykładowe gatunki książek
INSERT INTO genres (id, name, description)
VALUES
    ('660e8400-e29b-41d4-a716-446655440000', 'Fantasy', 'Literatura fantastyczna, światy pełne magii'),
    ('660e8400-e29b-41d4-a716-446655440001', 'Kryminał', 'Powieści kryminalne i thrillery'),
    ('660e8400-e29b-41d4-a716-446655440002', 'Romans', 'Literatura romantyczna'),
    ('660e8400-e29b-41d4-a716-446655440003', 'Science Fiction', 'Fantastyka naukowa'),
    ('660e8400-e29b-41d4-a716-446655440004', 'Literatura klasyczna', 'Klasyka literatury światowej');

-- Przykładowi autorzy
INSERT INTO authors (id, name, surname)
VALUES
    ('770e8400-e29b-41d4-a716-446655440000', 'J.K.', 'Rowling'),
    ('770e8400-e29b-41d4-a716-446655440001', 'Andrzej', 'Sapkowski'),
    ('770e8400-e29b-41d4-a716-446655440002', 'Agatha', 'Christie');

-- Przykładowe książki
INSERT INTO books (id, title, isbn, description, cover_image_url, publication_year, language, average_rating, total_reviews, author_id, genre_id)
VALUES
    ('880e8400-e29b-41d4-a716-446655440000', 'Harry Potter i Kamień Filozoficzny', '978-83-7686-415-4', 'Pierwsza część przygód młodego czarodzieja', 'https://s.lubimyczytac.pl/upload/books/323000/323299/524151-352x500.jpg', 1997, 'polski', 4.5, 0, '770e8400-e29b-41d4-a716-446655440000', '660e8400-e29b-41d4-a716-446655440000'),
    ('880e8400-e29b-41d4-a716-446655440001', 'Wiedźmin: Ostatnie życzenie', '978-83-7469-052-0', 'Zbiór opowiadań o wiedźminie Geralcie', 'https://s.lubimyczytac.pl/upload/books/4900000/4900233/749061-352x500.jpg', 1993, 'polski', 4.7, 0, '770e8400-e29b-41d4-a716-446655440001', '660e8400-e29b-41d4-a716-446655440000');