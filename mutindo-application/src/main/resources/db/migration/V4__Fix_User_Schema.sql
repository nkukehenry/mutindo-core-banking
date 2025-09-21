-- Fix User table schema to match entity fields
-- Add missing columns that don't exist yet

-- Add missing columns that exist in the User entity but not in database
ALTER TABLE users ADD COLUMN password_changed_at TIMESTAMP NULL;
ALTER TABLE users ADD COLUMN must_change_password BOOLEAN DEFAULT FALSE;

-- Rename account_locked to locked to match entity field name
ALTER TABLE users CHANGE COLUMN account_locked locked BOOLEAN DEFAULT FALSE;
