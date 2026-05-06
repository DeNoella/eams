-- Seed predefined hosting locations for the default organisation so the
-- Asset registration form can offer concrete choices (Remera/Goshen, BNR …)
-- instead of a blank dropdown. ON CONFLICT DO NOTHING keeps the migration
-- idempotent for existing databases.

INSERT INTO locations (id, organisation_id, location_type, name, code, address)
VALUES
    ('00000000-0000-0000-0004-000000000001', '00000000-0000-0000-0000-000000000001', 'BRANCH', 'Remera/Goshen', 'REM', 'Remera, Kigali, Rwanda'),
    ('00000000-0000-0000-0004-000000000002', '00000000-0000-0000-0000-000000000001', 'BRANCH', 'BNR Data Centre', 'BNR', 'National Bank of Rwanda, Kigali'),
    ('00000000-0000-0000-0004-000000000003', '00000000-0000-0000-0000-000000000001', 'BRANCH', 'Nyarugenge', 'NYR', 'Nyarugenge, Kigali, Rwanda'),
    ('00000000-0000-0000-0004-000000000004', '00000000-0000-0000-0000-000000000001', 'BRANCH', 'Kicukiro', 'KCK', 'Kicukiro, Kigali, Rwanda'),
    ('00000000-0000-0000-0004-000000000005', '00000000-0000-0000-0000-000000000001', 'BRANCH', 'Musanze', 'MSZ', 'Musanze, Rwanda'),
    ('00000000-0000-0000-0004-000000000006', '00000000-0000-0000-0000-000000000001', 'BRANCH', 'Huye', 'HUY', 'Huye, Rwanda'),
    ('00000000-0000-0000-0004-000000000007', '00000000-0000-0000-0000-000000000001', 'BRANCH', 'Rubavu', 'RBV', 'Rubavu, Rwanda'),
    ('00000000-0000-0000-0004-000000000008', '00000000-0000-0000-0000-000000000001', 'BRANCH', 'Cloud (AWS)', 'AWS', 'Amazon Web Services — primary region'),
    ('00000000-0000-0000-0004-000000000009', '00000000-0000-0000-0000-000000000001', 'BRANCH', 'Cloud (Azure)', 'AZU', 'Microsoft Azure — primary region'),
    ('00000000-0000-0000-0004-00000000000A', '00000000-0000-0000-0000-000000000001', 'BRANCH', 'Disaster Recovery Site', 'DR', 'Off-site DR facility')
ON CONFLICT DO NOTHING;
