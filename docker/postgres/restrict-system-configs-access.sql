-- ============================================================================
-- SQL Script: Restrict Access to system_configs Table
-- ============================================================================
-- This script sets up access restrictions for the system_configs table
-- which contains sensitive data like RSA private keys.
--
-- Run this script in PRODUCTION to enhance security.
-- ============================================================================

-- Connect to marketplace_member database
\c marketplace_member;

-- 1. Create a dedicated role for the application
-- This role will have limited permissions
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'member_service_app') THEN
        CREATE ROLE member_service_app WITH LOGIN PASSWORD 'change_this_in_production';
    END IF;
END
$$;

-- 2. Revoke all permissions from PUBLIC on sensitive tables
REVOKE ALL ON TABLE system_configs FROM PUBLIC;

-- 3. Grant SELECT only to the application role
-- The app only needs to READ the configs, not modify them
GRANT SELECT ON TABLE system_configs TO member_service_app;

-- 4. For seeding/admin operations, use a separate admin role
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'member_service_admin') THEN
        CREATE ROLE member_service_admin WITH LOGIN PASSWORD 'change_this_admin_password';
    END IF;
END
$$;

-- Admin can INSERT/UPDATE for seeding, but not DELETE (for audit trail)
GRANT SELECT, INSERT, UPDATE ON TABLE system_configs TO member_service_admin;
GRANT USAGE, SELECT ON SEQUENCE system_configs_id_seq TO member_service_admin;

-- 5. Enable Row Level Security (optional, for multi-tenant scenarios)
-- ALTER TABLE system_configs ENABLE ROW LEVEL SECURITY;

-- 6. Create audit log trigger for tracking changes to sensitive configs
CREATE TABLE IF NOT EXISTS system_configs_audit (
    id SERIAL PRIMARY KEY,
    config_id BIGINT NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    action VARCHAR(10) NOT NULL, -- INSERT, UPDATE, DELETE
    old_value TEXT,
    new_value TEXT,
    changed_by VARCHAR(100) DEFAULT current_user,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create audit trigger function
CREATE OR REPLACE FUNCTION audit_system_configs()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO system_configs_audit (config_id, config_key, action, new_value)
        VALUES (NEW.id, NEW.config_key, 'INSERT', 
                CASE WHEN NEW.config_key LIKE '%PRIVATE%' THEN '[REDACTED]' ELSE NEW.config_value END);
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO system_configs_audit (config_id, config_key, action, old_value, new_value)
        VALUES (OLD.id, OLD.config_key, 'UPDATE',
                CASE WHEN OLD.config_key LIKE '%PRIVATE%' THEN '[REDACTED]' ELSE OLD.config_value END,
                CASE WHEN NEW.config_key LIKE '%PRIVATE%' THEN '[REDACTED]' ELSE NEW.config_value END);
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO system_configs_audit (config_id, config_key, action, old_value)
        VALUES (OLD.id, OLD.config_key, 'DELETE',
                CASE WHEN OLD.config_key LIKE '%PRIVATE%' THEN '[REDACTED]' ELSE OLD.config_value END);
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Attach trigger to system_configs table
DROP TRIGGER IF EXISTS system_configs_audit_trigger ON system_configs;
CREATE TRIGGER system_configs_audit_trigger
    AFTER INSERT OR UPDATE OR DELETE ON system_configs
    FOR EACH ROW EXECUTE FUNCTION audit_system_configs();

-- 7. Protect audit table - only SELECT allowed
REVOKE ALL ON TABLE system_configs_audit FROM PUBLIC;
GRANT SELECT ON TABLE system_configs_audit TO member_service_admin;

-- ============================================================================
-- Usage Notes:
-- ============================================================================
-- In production, update docker-compose.yml or application.yml to use:
--   POSTGRES_USER: member_service_app
--   POSTGRES_PASSWORD: <secure_password>
--
-- For seeding operations (first run), temporarily use admin role:
--   POSTGRES_USER: member_service_admin
--   POSTGRES_PASSWORD: <admin_password>
-- ============================================================================

-- Display created objects
\echo '=== Access Restrictions Applied ==='
\echo 'Roles created: member_service_app, member_service_admin'
\echo 'Audit table: system_configs_audit'
\echo 'Trigger: system_configs_audit_trigger'
\echo ''
\echo 'IMPORTANT: Change default passwords in production!'

