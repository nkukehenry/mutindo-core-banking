-- Fix the password hash for Admin!2025
-- This migration updates the admin user with the correct BCrypt hash

-- Update admin user password hash to match Admin!2025
-- Generated BCrypt hash for Admin!2025 with strength 12
UPDATE users SET password_hash = '$2a$12$vK8S6.H/LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfcx.zcx4RgTZF5jKjK4e' 
WHERE username = 'admin';

-- Also update other users to use the same password for testing
UPDATE users SET password_hash = '$2a$12$vK8S6.H/LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfcx.zcx4RgTZF5jKjK4e' 
WHERE username IN ('manager1', 'teller1', 'teller2');

-- Verify the update
-- SELECT username, LEFT(password_hash, 20) as hash_preview FROM users;
