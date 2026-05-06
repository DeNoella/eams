-- Add dynamic category-specific columns to assets so Application / Database /
-- Server / Equipment registrations can persist their unique fields. All
-- columns are nullable so existing rows remain valid under ddl-auto=validate.

ALTER TABLE assets
    ADD COLUMN IF NOT EXISTS version_no VARCHAR(60),
    ADD COLUMN IF NOT EXISTS version_date DATE,
    ADD COLUMN IF NOT EXISTS number_of_licences INTEGER,
    ADD COLUMN IF NOT EXISTS hosting_institution VARCHAR(255),
    ADD COLUMN IF NOT EXISTS functions_of_system TEXT,
    ADD COLUMN IF NOT EXISTS staff_in_charge VARCHAR(255),
    ADD COLUMN IF NOT EXISTS server_type VARCHAR(60),
    ADD COLUMN IF NOT EXISTS operating_system_name VARCHAR(120),
    ADD COLUMN IF NOT EXISTS os_vendor_name VARCHAR(120),
    ADD COLUMN IF NOT EXISTS os_version_no VARCHAR(60),
    ADD COLUMN IF NOT EXISTS model_number VARCHAR(120),
    ADD COLUMN IF NOT EXISTS equipment_licence_type VARCHAR(60);

-- Seed an Equipment asset category for the default organisation so the
-- dynamic Equipment form described in the spec can be selected immediately.
INSERT INTO asset_categories (
    id, organisation_id, name, code, description, id_format,
    is_physical, requires_location, sort_order
)
VALUES (
    '00000000-0000-0000-0002-000000000006',
    '00000000-0000-0000-0000-000000000001',
    'Equipment',
    'EQP',
    'Physical IT equipment (network, client, peripherals)',
    '{CODE}-{LOC}-{YYYY}-{SEQ:4}',
    TRUE,
    TRUE,
    6
)
ON CONFLICT DO NOTHING;
