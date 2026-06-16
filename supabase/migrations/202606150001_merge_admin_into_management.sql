BEGIN;

INSERT INTO roles (name, description, created_at, updated_at)
VALUES
    ('MANAGEMENT', 'Pengelolaan penuh sistem, bisnis, user, dan laporan', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('GUDANG', 'Pengelolaan stok dan bahan baku', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('KASIR', 'Transaksi penjualan', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP;

UPDATE users
SET role_id = (SELECT id FROM roles WHERE name = 'MANAGEMENT'),
    updated_at = CURRENT_TIMESTAMP
WHERE role_id IN (
    SELECT id
    FROM roles
    WHERE name IN ('ADMIN', 'MANAJEMEN')
);

DELETE FROM roles
WHERE name IN ('ADMIN', 'MANAJEMEN');

COMMIT;
