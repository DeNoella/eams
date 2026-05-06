-- AI MODELS TABLE
CREATE TABLE ai_models (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    model_name VARCHAR(150) NOT NULL,
    model_type VARCHAR(50) NOT NULL,
    model_version VARCHAR(30) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    endpoint_url TEXT,
    api_key_secret_ref TEXT,
    input_features JSONB NOT NULL,
    output_schema JSONB NOT NULL,
    training_dataset_ref TEXT,
    accuracy_metric NUMERIC(5,4),
    threshold_config JSONB,
    retrain_schedule VARCHAR(50),
    last_trained_at TIMESTAMPTZ,
    last_run_at TIMESTAMPTZ,
    is_active BOOLEAN DEFAULT TRUE,
    is_approved BOOLEAN DEFAULT FALSE,
    approved_by_user_id UUID REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- AI PREDICTIONS TABLE
CREATE TABLE ai_predictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    model_id UUID NOT NULL REFERENCES ai_models(id),
    asset_id UUID REFERENCES assets(id),
    prediction_type VARCHAR(100) NOT NULL,
    input_snapshot JSONB NOT NULL,
    prediction_output JSONB NOT NULL,
    confidence_score NUMERIC(5,4),
    risk_band VARCHAR(20),
    predicted_value TEXT,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    valid_until TIMESTAMPTZ,
    actioned_at TIMESTAMPTZ,
    actioned_by_user_id UUID REFERENCES users(id),
    outcome VARCHAR(30),
    feedback_note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- AI ANOMALIES TABLE
CREATE TABLE ai_anomalies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    model_id UUID NOT NULL REFERENCES ai_models(id),
    anomaly_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    affected_resource_type VARCHAR(100),
    affected_resource_id UUID,
    anomaly_score NUMERIC(5,4),
    supporting_evidence JSONB,
    detected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    acknowledged_at TIMESTAMPTZ,
    acknowledged_by_user_id UUID REFERENCES users(id),
    resolution TEXT,
    resolved_at TIMESTAMPTZ,
    false_positive BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- AI RECOMMENDATIONS TABLE
CREATE TABLE ai_recommendations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    model_id UUID NOT NULL REFERENCES ai_models(id),
    recommendation_type VARCHAR(100) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    estimated_saving NUMERIC(15,2),
    estimated_risk_reduction TEXT,
    affected_asset_ids UUID[],
    supporting_data JSONB,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ,
    actioned_at TIMESTAMPTZ,
    actioned_by_user_id UUID REFERENCES users(id),
    action_taken TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- AI CHAT SESSIONS TABLE
CREATE TABLE ai_chat_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    user_id UUID NOT NULL REFERENCES users(id),
    session_title VARCHAR(255),
    messages JSONB NOT NULL DEFAULT '[]',
    context_asset_ids UUID[],
    model_used VARCHAR(100) NOT NULL,
    total_tokens_used INTEGER DEFAULT 0,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_activity_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_archived BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- INDEXES
CREATE INDEX idx_ai_predictions_organisation_id ON ai_predictions(organisation_id);
CREATE INDEX idx_ai_predictions_asset_id ON ai_predictions(asset_id);
CREATE INDEX idx_ai_anomalies_organisation_id ON ai_anomalies(organisation_id);
CREATE INDEX idx_ai_anomalies_status ON ai_anomalies(status);
CREATE INDEX idx_ai_chat_sessions_user_id ON ai_chat_sessions(user_id);