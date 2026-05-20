SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE users (

                       id CHAR(36) NOT NULL,

                       email VARCHAR(255) NOT NULL,

                       password_hash VARCHAR(255) DEFAULT NULL,

                       first_name VARCHAR(100) DEFAULT NULL,

                       last_name VARCHAR(100) DEFAULT NULL,

                       is_enabled TINYINT(1) NOT NULL DEFAULT 1,

                       is_account_non_locked TINYINT(1) NOT NULL DEFAULT 1,

                       auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',

                       external_provider_id VARCHAR(255) DEFAULT NULL,

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,

                       PRIMARY KEY (id),

                       UNIQUE KEY uk_users_email (email),

                       CONSTRAINT chk_users_provider
                           CHECK (
                               auth_provider IN (
                                                 'LOCAL',
                                                 'GOOGLE',
                                                 'FACEBOOK',
                                                 'GITHUB'
                                   )
                               ),

                       CONSTRAINT chk_users_flags
                           CHECK (
                               is_enabled IN (0,1)
                                   AND
                               is_account_non_locked IN (0,1)
                               ),

                       INDEX idx_users_email (email),

                       INDEX idx_users_provider_lookup (
        auth_provider,
        external_provider_id
    )

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='Central user registry for authentication & profile management';



CREATE TABLE roles (

                       id CHAR(36) NOT NULL,

                       role_code VARCHAR(50) NOT NULL,

                       description VARCHAR(255) DEFAULT NULL,

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       PRIMARY KEY (id),

                       UNIQUE KEY uk_roles_code (role_code),

                       INDEX idx_roles_code (role_code)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='RBAC role definitions';



CREATE TABLE permissions (

                             id CHAR(36) NOT NULL,

                             permission_code VARCHAR(50) NOT NULL,

                             resource_type VARCHAR(50) DEFAULT NULL,

                             action_type VARCHAR(20) DEFAULT NULL,

                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             PRIMARY KEY (id),

                             UNIQUE KEY uk_permissions_code (permission_code),

                             INDEX idx_permissions_code (permission_code)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='Granular permission definitions';



CREATE TABLE user_role_mappings (

                                    user_id CHAR(36) NOT NULL,

                                    role_id CHAR(36) NOT NULL,

                                    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    assigned_by CHAR(36) DEFAULT NULL,

                                    PRIMARY KEY (user_id, role_id),

                                    CONSTRAINT fk_urm_user
                                        FOREIGN KEY (user_id)
                                            REFERENCES users(id)
                                            ON DELETE CASCADE,

                                    CONSTRAINT fk_urm_role
                                        FOREIGN KEY (role_id)
                                            REFERENCES roles(id)
                                            ON DELETE CASCADE,

                                    INDEX idx_urm_user (user_id),

                                    INDEX idx_urm_role (role_id)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='Junction: users <-> roles';



CREATE TABLE role_permission_mappings (

                                          role_id CHAR(36) NOT NULL,

                                          permission_id CHAR(36) NOT NULL,

                                          assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                          PRIMARY KEY (role_id, permission_id),

                                          CONSTRAINT fk_rpm_role
                                              FOREIGN KEY (role_id)
                                                  REFERENCES roles(id)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT fk_rpm_perm
                                              FOREIGN KEY (permission_id)
                                                  REFERENCES permissions(id)
                                                  ON DELETE CASCADE,

                                          INDEX idx_rpm_role (role_id),

                                          INDEX idx_rpm_perm (permission_id)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='Junction: roles <-> permissions';



CREATE TABLE refresh_tokens (

                                id CHAR(36) NOT NULL,

                                user_id CHAR(36) NOT NULL,

                                token_value VARCHAR(255) NOT NULL,

                                expires_at TIMESTAMP NOT NULL,

                                is_revoked TINYINT(1) NOT NULL DEFAULT 0,

                                client_ip VARCHAR(45) DEFAULT NULL,

                                user_agent_string VARCHAR(500) DEFAULT NULL,

                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                revoked_at TIMESTAMP NULL DEFAULT NULL,

                                PRIMARY KEY (id),

                                UNIQUE KEY uk_tokens_value (token_value),

                                CONSTRAINT fk_rt_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT chk_tokens_revoked
                                    CHECK (is_revoked IN (0,1)),

                                INDEX idx_rt_user (user_id),

                                INDEX idx_rt_expiry (expires_at),

                                INDEX idx_rt_revoked (is_revoked)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='JWT refresh token storage for rotation & cleanup jobs';

SET FOREIGN_KEY_CHECKS = 1;