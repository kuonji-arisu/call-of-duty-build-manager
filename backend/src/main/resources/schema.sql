CREATE TABLE IF NOT EXISTS app_settings (
    setting_key VARCHAR(100) NOT NULL PRIMARY KEY,
    setting_value TEXT NOT NULL,
    updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    username VARCHAR(80) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_users_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS weapons (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    weapon_type VARCHAR(64) NOT NULL,
    tags JSON NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS weapon_generations (
    weapon_id VARCHAR(64) NOT NULL,
    generation VARCHAR(32) NOT NULL,
    PRIMARY KEY (weapon_id, generation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS weapon_slots (
    weapon_id VARCHAR(64) NOT NULL,
    slot VARCHAR(64) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (weapon_id, slot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS attachments (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    subtitle VARCHAR(160) NOT NULL,
    slot VARCHAR(64) NOT NULL,
    tags JSON NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS attachment_weapons (
    attachment_id VARCHAR(64) NOT NULL,
    weapon_id VARCHAR(64) NOT NULL,
    PRIMARY KEY (attachment_id, weapon_id),
    KEY idx_attachment_weapons_weapon_id (weapon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS attachment_generations (
    attachment_id VARCHAR(64) NOT NULL,
    generation VARCHAR(32) NOT NULL,
    PRIMARY KEY (attachment_id, generation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS attachment_effect_definitions (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    label VARCHAR(120) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_attachment_effect_definition_label UNIQUE (label)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS attachment_effects (
    attachment_id VARCHAR(64) NOT NULL,
    definition_id VARCHAR(64) NOT NULL,
    effect_type VARCHAR(16) NOT NULL,
    level TINYINT NOT NULL,
    PRIMARY KEY (attachment_id, definition_id, effect_type),
    KEY idx_attachment_effects_definition_id (definition_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS builds (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    weapon_id VARCHAR(64) NOT NULL,
    name VARCHAR(120) NOT NULL,
    notes TEXT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    KEY idx_builds_weapon_id (weapon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS build_generations (
    build_id VARCHAR(64) NOT NULL,
    generation VARCHAR(32) NOT NULL,
    PRIMARY KEY (build_id, generation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS build_items (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    build_id VARCHAR(64) NOT NULL,
    slot VARCHAR(64) NOT NULL,
    attachment_id VARCHAR(64) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    KEY idx_build_items_build_id (build_id),
    KEY idx_build_items_attachment_id (attachment_id),
    CONSTRAINT uk_build_slot UNIQUE (build_id, slot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO app_settings (setting_key, setting_value, updated_at)
VALUES
    ('homeGenerationFilter', 'ALL', CURRENT_TIMESTAMP(6)),
    ('libraryTitle', 'COD Build Manager', CURRENT_TIMESTAMP(6));
