-- ACCESS GRANTS TABLE
CREATE TABLE access_grants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    access_type VARCHAR(50) NOT NULL,
    target_asset_id UUID REFERENCES assets(id),
    requestor_user_id UUID NOT NULL REFERENCES users(id),
    approving_user_id UUID NOT NULL REFERENCES users(id),
    business_justification TEXT NOT NULL,
    ticket_reference VARCHAR(100),
    risk_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    revoking_user_id UUID REFERENCES users(id),
    revocation_confirmation TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    overdue_escalated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_approver_not_requestor CHECK (approving_user_id != requestor_user_id)
);

-- CHANGE REQUESTS TABLE
CREATE TABLE change_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    change_reference VARCHAR(50) UNIQUE NOT NULL,
    target_asset_id UUID NOT NULL REFERENCES assets(id),
    change_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    business_justification TEXT NOT NULL,
    risk_assessment TEXT,
    revert_plan TEXT NOT NULL,
    proposed_by_user_id UUID NOT NULL REFERENCES users(id),
    approved_by_user_id UUID REFERENCES users(id),
    implemented_by_user_id UUID REFERENCES users(id),
    verified_by_user_id UUID REFERENCES users(id),
    scheduled_start TIMESTAMPTZ,
    scheduled_revert_date DATE NOT NULL,
    actual_applied_at TIMESTAMPTZ,
    actual_reverted_at TIMESTAMPTZ,
    status VARCHAR(30) NOT NULL DEFAULT 'PROPOSED',
    overdue_escalated_at TIMESTAMPTZ,
    made_permanent_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_approver_not_proposer CHECK (approved_by_user_id != proposed_by_user_id)
);

-- INDEXES
CREATE INDEX idx_access_grants_organisation_id ON access_grants(organisation_id);
CREATE INDEX idx_access_grants_status ON access_grants(status);
CREATE INDEX idx_access_grants_expires_at ON access_grants(expires_at);
CREATE INDEX idx_change_requests_organisation_id ON change_requests(organisation_id);
CREATE INDEX idx_change_requests_status ON change_requests(status);
CREATE INDEX idx_change_requests_scheduled_revert ON change_requests(scheduled_revert_date);