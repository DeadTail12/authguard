CREATE TABLE roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE permissions (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- There is no "users" table here on purpose: identity belongs to Keycloak.
-- This just maps a Keycloak subject id (JWT "sub") to the roles AuthGuard grants it.
CREATE TABLE user_roles (
    id               BIGSERIAL PRIMARY KEY,
    keycloak_user_id VARCHAR(36) NOT NULL,
    role_id          BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE (keycloak_user_id, role_id)
);
CREATE INDEX idx_user_roles_keycloak_user_id ON user_roles (keycloak_user_id);

INSERT INTO permissions (name) VALUES
    ('users:read'),
    ('users:manage'),
    ('orders:read'),
    ('orders:manage'),
    ('policies:manage');

INSERT INTO roles (name) VALUES ('ADMIN'), ('MANAGER'), ('USER');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'MANAGER' AND p.name IN ('orders:read', 'orders:manage');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'USER' AND p.name = 'users:read';

-- Dev/demo seed only: wires the two Keycloak test users (see keycloak/realm-export.json,
-- fixed "id" fields) to roles so the whole flow is testable end-to-end out of the box.
-- A real deployment would assign roles through an admin API/console, not a migration.
INSERT INTO user_roles (keycloak_user_id, role_id)
SELECT '11111111-1111-4111-8111-111111111111', id FROM roles WHERE name = 'ADMIN';

INSERT INTO user_roles (keycloak_user_id, role_id)
SELECT '22222222-2222-4222-8222-222222222222', id FROM roles WHERE name = 'MANAGER';
