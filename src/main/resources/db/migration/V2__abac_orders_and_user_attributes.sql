-- MANAGER's order access becomes region-scoped (ABAC), not blanket (that was only useful
-- for the RBAC demo). ADMIN keeps orders:manage and stays a full-access override.
DELETE FROM role_permissions
WHERE role_id = (SELECT id FROM roles WHERE name = 'MANAGER')
  AND permission_id = (SELECT id FROM permissions WHERE name = 'orders:manage');

CREATE TABLE user_attributes (
    keycloak_user_id VARCHAR(36) PRIMARY KEY,
    region           VARCHAR(50) NOT NULL
);

CREATE TABLE orders (
    id                     BIGSERIAL PRIMARY KEY,
    region                 VARCHAR(50) NOT NULL,
    owner_keycloak_user_id VARCHAR(36) NOT NULL,
    description            VARCHAR(200) NOT NULL
);

-- Dev/demo seed, mirrors the Keycloak test users in keycloak/realm-export.json.
-- bob (MANAGER) is EU-WEST; alice (ADMIN) needs no region since orders:manage bypasses ABAC.
INSERT INTO user_attributes (keycloak_user_id, region) VALUES
    ('22222222-2222-4222-8222-222222222222', 'EU-WEST');

-- id 1: same region as bob, owned by someone else      -> bob allowed via SAME_REGION
-- id 2: different region, but owned by bob              -> bob allowed via RESOURCE_OWNER
-- id 3: different region, owned by someone else         -> bob denied
INSERT INTO orders (region, owner_keycloak_user_id, description) VALUES
    ('EU-WEST', '33333333-3333-4333-8333-333333333333', 'Order in bob''s region, owned by someone else'),
    ('EU-EAST', '22222222-2222-4222-8222-222222222222', 'Order outside bob''s region, but owned by bob'),
    ('EU-EAST', '33333333-3333-4333-8333-333333333333', 'Order outside bob''s region, owned by someone else');
