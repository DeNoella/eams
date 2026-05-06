-- CHECK IN/OUT TRANSACTIONS TABLE
CREATE TABLE check_in_out_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    asset_id UUID NOT NULL REFERENCES assets(id),
    transaction_type VARCHAR(20) NOT NULL,
    from_user_id UUID REFERENCES users(id),
    to_user_id UUID REFERENCES users(id),
    from_location_id UUID REFERENCES locations(id),
    to_location_id UUID REFERENCES locations(id),
    from_department_id UUID REFERENCES departments(id),
    to_department_id UUID REFERENCES departments(id),
    transaction_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expected_return_date DATE,
    actual_return_date DATE,
    authorising_user_id UUID NOT NULL REFERENCES users(id),
    purpose TEXT,
    condition_on_out VARCHAR(30),
    condition_on_in VARCHAR(30),
    incident_ticket_ref VARCHAR(100),
    notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    overdue_notified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- CERTIFICATES TABLE
CREATE TABLE certificates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id UUID NOT NULL REFERENCES assets(id),
    certificate_type VARCHAR(50) NOT NULL,
    issuing_authority VARCHAR(255) NOT NULL,
    subject_dn TEXT NOT NULL,
    common_name VARCHAR(255) NOT NULL,
    san_entries TEXT[],
    serial_number VARCHAR(100) NOT NULL,
    fingerprint_sha256 CHAR(64) NOT NULL,
    algorithm VARCHAR(30) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    renewal_lead_days INTEGER DEFAULT 90,
    auto_renewal_enabled BOOLEAN DEFAULT FALSE,
    environment VARCHAR(20) NOT NULL DEFAULT 'PRODUCTION',
    key_store_location TEXT,
    owner_user_id UUID REFERENCES users(id),
    previous_cert_id UUID REFERENCES certificates(id),
    renewed_by_user_id UUID REFERENCES users(id),
    renewed_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL DEFAULT 'VALID',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- LICENCES TABLE
CREATE TABLE licences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id UUID NOT NULL REFERENCES assets(id),
    licence_type VARCHAR(30) NOT NULL,
    total_seats INTEGER,
    used_seats INTEGER DEFAULT 0,
    expiry_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'VALID',
    renewal_lead_days INTEGER DEFAULT 90,
    software_assurance_expiry DATE,
    key_or_reference TEXT,
    seat_alert_threshold_pct SMALLINT DEFAULT 85,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- INDEXES
CREATE INDEX idx_certificates_asset_id ON certificates(asset_id);
CREATE INDEX idx_certificates_expiry_date ON certificates(expiry_date);
CREATE INDEX idx_certificates_status ON certificates(status);
CREATE INDEX idx_licences_asset_id ON licences(asset_id);
CREATE INDEX idx_licences_expiry_date ON licences(expiry_date);
CREATE INDEX idx_licences_status ON licences(status);
CREATE INDEX idx_transactions_asset_id ON check_in_out_transactions(asset_id);
CREATE INDEX idx_transactions_status ON check_in_out_transactions(status);