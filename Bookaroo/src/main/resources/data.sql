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

-- 1. Autorzy
INSERT INTO authors (id, name, surname) VALUES
                                            ('11111111-1111-1111-1111-111111111111', 'J.R.R.', 'Tolkien'),
                                            ('22222222-2222-2222-2222-222222222222', 'George', 'Orwell');

-- Książki
INSERT INTO books (id, title, isbn, description, publication_year, author_id) VALUES
                                                                                  ('33333333-3333-3333-3333-333333333333', 'Władca Pierścieni', '978-1234567890', 'Epicka opowieść.', 1954, '11111111-1111-1111-1111-111111111111'),
                                                                                  ('44444444-4444-4444-4444-444444444444', 'Rok 1984', '978-0987654321', 'Wielki Brat patrzy.', 1949, '22222222-2222-2222-2222-222222222222');
