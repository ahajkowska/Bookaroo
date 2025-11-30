-- Przykładowi użytkownicy
INSERT INTO users (id, username, email, password, role, first_name, last_name, reputation_score)
VALUES
    ('550e8400-e29b-41d4-a716-446655440000', 'adammalysz', 'adammalysz@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 'John', 'Doe', 50),
    ('550e8400-e29b-41d4-a716-446655440001', 'magdagessler', 'magdagesslet@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 'Jane', 'Doe', 85);

-- Przykładowe kategorie
INSERT INTO categories (id, name, description)
VALUES
    ('660e8400-e29b-41d4-a716-446655440000', 'Ogrodnictwo', 'Pomoc w pracach ogrodowych'),
    ('660e8400-e29b-41d4-a716-446655440001', 'Transport', 'Pomoc w transporcie rzeczy'),
    ('660e8400-e29b-41d4-a716-446655440002', 'Naprawa', 'Drobne naprawy');