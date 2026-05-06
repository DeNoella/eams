-- ASSET CATEGORIES TABLE
CREATE TABLE asset_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL,
    icon VARCHAR(50),
    description TEXT,
    id_format VARCHAR(100) NOT NULL DEFAULT '{CODE}-{LOC}-{YYYY}-{SEQ:4}',
    default_criticality VARCHAR(20) DEFAULT 'MEDIUM',
    depreciation_years INTEGER,
    is_physical BOOLEAN DEFAULT FALSE,
    requires_location BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- CUSTOM FIELDS TABLE
CREATE TABLE custom_fields (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    asset_category_id UUID NOT NULL REFERENCES asset_categories(id),
    field_label VARCHAR(150) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_type VARCHAR(30) NOT NULL,
    options JSONB,
    is_mandatory BOOLEAN DEFAULT FALSE,
    is_searchable BOOLEAN DEFAULT TRUE,
    default_value TEXT,
    validation_regex TEXT,
    help_text TEXT,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- ASSETS CORE TABLE
CREATE TABLE assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    asset_id_display VARCHAR(60) NOT NULL,
    asset_category_id UUID NOT NULL REFERENCES asset_categories(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    location_id UUID REFERENCES locations(id),
    department_id UUID REFERENCES departments(id),
    assigned_user_id UUID REFERENCES users(id),
    technical_owner_id UUID REFERENCES users(id),
    business_owner_id UUID REFERENCES users(id),
    backup_owner_id UUID REFERENCES users(id),
    criticality VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    year_of_installation SMALLINT,
    acquisition_cost NUMERIC(15,2),
    currency_code CHAR(3) DEFAULT 'USD',
    annual_cost NUMERIC(15,2),
    purchase_order_ref VARCHAR(100),
    vendor_name VARCHAR(255),
    support_contract_ref VARCHAR(100),
    support_expiry_date DATE,
    warranty_expiry_date DATE,
    last_audit_date DATE,
    next_audit_due_date DATE,
    notes TEXT,
    qr_code_url TEXT,
    barcode VARCHAR(100),
    lifecycle_stage VARCHAR(50) DEFAULT 'IN_SERVICE',
    decommission_date DATE,
    decommission_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (organisation_id, asset_id_display)
);

-- ASSET CUSTOM VALUES TABLE
CREATE TABLE asset_custom_values (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id UUID NOT NULL REFERENCES assets(id),
    custom_field_id UUID NOT NULL REFERENCES custom_fields(id),
    field_value TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(asset_id, custom_field_id)
);

-- ASSET RELATIONSHIPS TABLE (CMDB)
CREATE TABLE asset_relationships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    source_asset_id UUID NOT NULL REFERENCES assets(id),
    target_asset_id UUID NOT NULL REFERENCES assets(id),
    relationship_type VARCHAR(50) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- ASSET ATTACHMENTS TABLE
CREATE TABLE asset_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id UUID NOT NULL REFERENCES assets(id),
    file_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    file_size_bytes BIGINT,
    uploaded_by_user_id UUID REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- INDEXES
CREATE INDEX idx_assets_organisation_id ON assets(organisation_id);
CREATE INDEX idx_assets_asset_category_id ON assets(asset_category_id);
CREATE INDEX idx_assets_status ON assets(status);
CREATE INDEX idx_assets_criticality ON assets(criticality);
CREATE INDEX idx_assets_location_id ON assets(location_id);
CREATE INDEX idx_assets_department_id ON assets(department_id);
CREATE INDEX idx_assets_assigned_user_id ON assets(assigned_user_id);
CREATE INDEX idx_asset_relationships_source ON asset_relationships(source_asset_id);
CREATE INDEX idx_asset_relationships_target ON asset_relationships(target_asset_id);