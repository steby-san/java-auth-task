INSERT INTO roles (
    id,
    role_code,
    description
)
VALUES
    (
        UUID(),
        'ROLE_USER',
        'Default user role'
    ),
    (
        UUID(),
        'ROLE_ADMIN',
        'Administrator role'
    );



INSERT INTO permissions (
    id,
    permission_code,
    resource_type,
    action_type
)
VALUES
    (
        UUID(),
        'user:read',
        'USER',
        'READ'
    ),
    (
        UUID(),
        'user:write',
        'USER',
        'WRITE'
    ),
    (
        UUID(),
        'admin:delete',
        'ADMIN',
        'DELETE'
    );