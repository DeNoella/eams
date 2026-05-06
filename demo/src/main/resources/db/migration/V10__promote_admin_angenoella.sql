-- Promote angenoella1771@gmail.com to SYSTEM_ADMIN in the default organisation.
-- Creates the user record if it doesn't exist yet, then grants the role idempotently.

INSERT INTO users (id, organisation_id, employee_id, full_name, email, job_title, is_active)
VALUES (
    '00000000-0000-0000-0003-000000000002',
    '00000000-0000-0000-0000-000000000001',
    'EMP-ADM-002',
    'Angeno Ella',
    'angenoella1771@gmail.com',
    'System Administrator',
    true
)
ON CONFLICT (email) DO UPDATE
    SET is_active = TRUE,
        employment_status = 'ACTIVE',
        job_title = COALESCE(users.job_title, EXCLUDED.job_title);

-- Grant SYSTEM_ADMIN role (and keep ASSET_MANAGER for convenience) using the
-- user's actual id, whatever it is (handles both newly-inserted and pre-existing rows).
INSERT INTO user_roles (user_id, role_id, assigned_by_user_id)
SELECT u.id, r.id, u.id
FROM users u
CROSS JOIN roles r
WHERE u.email = 'angenoella1771@gmail.com'
  AND r.code IN ('SYSTEM_ADMIN', 'ASSET_MANAGER')
  AND r.organisation_id = u.organisation_id
ON CONFLICT (user_id, role_id) DO NOTHING;
