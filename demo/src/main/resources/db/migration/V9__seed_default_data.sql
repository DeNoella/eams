-- Insert a default system organisation for initial setup
INSERT INTO organisations (id, name, slug, timezone, country_code, subscription_plan, locale)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Default Organisation',
    'default',
    'UTC',
    'US',
    'ENTERPRISE',
    'en'
) ON CONFLICT DO NOTHING;

-- Insert default roles for default organisation
INSERT INTO roles (id, organisation_id, name, code, description, is_system_role, requires_mfa, session_timeout_minutes)
VALUES
('00000000-0000-0000-0001-000000000001', '00000000-0000-0000-0000-000000000001', 'System Admin', 'SYSTEM_ADMIN', 'Full technical access; cannot modify audit trail', true, true, 15),
('00000000-0000-0000-0001-000000000002', '00000000-0000-0000-0000-000000000001', 'Asset Manager', 'ASSET_MANAGER', 'Manages inventory records and assignments only', true, true, 30),
('00000000-0000-0000-0001-000000000003', '00000000-0000-0000-0000-000000000001', 'Security Officer', 'SECURITY_OFFICER', 'Manages access grants and certificates', true, true, 15),
('00000000-0000-0000-0001-000000000004', '00000000-0000-0000-0000-000000000001', 'Change Manager', 'CHANGE_MANAGER', 'Approves changes; cannot also be the implementer', true, true, 30),
('00000000-0000-0000-0001-000000000005', '00000000-0000-0000-0000-000000000001', 'Auditor', 'AUDITOR', 'Read-only access to all data; full audit log export', true, true, 30),
('00000000-0000-0000-0001-000000000006', '00000000-0000-0000-0000-000000000001', 'Branch Staff', 'BRANCH_STAFF', 'Registers and views assets in their branch only', true, false, 60),
('00000000-0000-0000-0001-000000000007', '00000000-0000-0000-0000-000000000001', 'Procurement Officer', 'PROCUREMENT_OFFICER', 'Views and updates contract, cost, and licence data', true, false, 60),
('00000000-0000-0000-0001-000000000008', '00000000-0000-0000-0000-000000000001', 'AI Analyst', 'AI_ANALYST', 'Accesses AI dashboards, model outputs, and recommendations', true, false, 60),
('00000000-0000-0000-0001-000000000009', '00000000-0000-0000-0000-000000000001', 'Read-Only Viewer', 'READ_ONLY_VIEWER', 'Dashboard viewing only — no data modification rights', true, false, 60)
ON CONFLICT DO NOTHING;

-- Insert a test admin user
INSERT INTO users (id, organisation_id, employee_id, full_name, email, job_title, is_active)
VALUES (
    '00000000-0000-0000-0003-000000000001',
    '00000000-0000-0000-0000-000000000001',
    'EMP001',
    'Admin User',
    'mutesideno@gmail.com',
    'System Administrator',
    true
) ON CONFLICT DO NOTHING;

-- Assign SYSTEM_ADMIN role to test user
INSERT INTO user_roles (user_id, role_id, assigned_by_user_id)
VALUES (
    '00000000-0000-0000-0003-000000000001',
    '00000000-0000-0000-0001-000000000001',
    '00000000-0000-0000-0003-000000000001'
) ON CONFLICT DO NOTHING;

-- Insert default asset categories
INSERT INTO asset_categories (id, organisation_id, name, code, description, id_format, is_physical, requires_location, sort_order)
VALUES
('00000000-0000-0000-0002-000000000001', '00000000-0000-0000-0000-000000000001', 'Software Applications', 'APP', 'Software applications and systems', '{CODE}-{LOC}-{YYYY}-{SEQ:4}', false, false, 1),
('00000000-0000-0000-0002-000000000002', '00000000-0000-0000-0000-000000000001', 'Databases', 'DB', 'Database management systems', '{CODE}-{LOC}-{YYYY}-{SEQ:4}', false, true, 2),
('00000000-0000-0000-0002-000000000003', '00000000-0000-0000-0000-000000000001', 'Servers', 'SRV', 'Physical and virtual servers', '{CODE}-{LOC}-{YYYY}-{SEQ:4}', true, true, 3),
('00000000-0000-0000-0002-000000000004', '00000000-0000-0000-0000-000000000001', 'Network Devices', 'NET', 'Network infrastructure devices', '{CODE}-{LOC}-{YYYY}-{SEQ:4}', true, true, 4),
('00000000-0000-0000-0002-000000000005', '00000000-0000-0000-0000-000000000001', 'Client Devices', 'DEV', 'End-user computing devices', '{CODE}-{LOC}-{YYYY}-{SEQ:4}', true, true, 5)
ON CONFLICT DO NOTHING;