-- ORGANISATIONS TABLE
CREATE TABLE organisations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    logo_url TEXT,
    primary_color VARCHAR(7) DEFAULT '#1B3A6B',
    timezone VARCHAR(64) NOT NULL,
    locale VARCHAR(10) DEFAULT 'en',
    country_code CHAR(2) NOT NULL,
    subscription_plan VARCHAR(50) DEFAULT 'standard',
    max_assets INTEGER DEFAULT 10000,
    is_active BOOLEAN DEFAULT TRUE,
    settings JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- LOCATIONS TABLE (self-referencing hierarchy)
CREATE TABLE locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    parent_id UUID REFERENCES locations(id),
    location_type VARCHAR(50) NOT NULL CHECK (location_type IN ('COUNTRY','REGION','BRANCH','BUILDING','FLOOR','ROOM','RACK')),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(20) NOT NULL,
    address TEXT,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    contact_email VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- DEPARTMENTS TABLE (self-referencing)
CREATE TABLE departments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    location_id UUID REFERENCES locations(id),
    parent_id UUID REFERENCES departments(id),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(20) NOT NULL,
    head_user_id UUID,
    cost_centre VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- INDEXES
CREATE INDEX idx_locations_organisation_id ON locations(organisation_id);
CREATE INDEX idx_locations_parent_id ON locations(parent_id);
CREATE INDEX idx_departments_organisation_id ON departments(organisation_id);
CREATE INDEX idx_departments_location_id ON departments(location_id);