CREATE TABLE items (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000), -- Usamos VARCHAR en lugar de TEXT para H2
    price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE offers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    item_id UUID NOT NULL,
    FOREIGN KEY(item_id) REFERENCES items(id) ON DELETE CASCADE
);

INSERT INTO items (id, name, description, price) VALUES
(RANDOM_UUID(), 'Gorra autografiada por Peso Pluma', 'Una gorra autografiada por el famoso Peso Pluma', 621.30),
(RANDOM_UUID(), 'Casco autografiado por Rosalía', 'Un casco autografiado por la famosa cantante Rosalía, una verdadera MOTOMAMI!', 734.57),
(RANDOM_UUID(), 'Chamarra de Bad Bunny', 'Una chamarra de la marca favorita de Bad Bunny, autografiada por el propio artista', 521.89),
(RANDOM_UUID(), 'Guitarra de Fernando Delgadillo', 'Una guitarra acústica de alta calidad utilizada por el famoso cantautor Fernando Delgadillo', 823.12),
(RANDOM_UUID(), 'Jersey firmado por Snoop Dogg', 'Un jersey autografiado por el legendario rapero Snoop Dogg', 355.67);