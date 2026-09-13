-- Migration: Add is_official column to listings table
ALTER TABLE listings ADD COLUMN is_official BOOLEAN NOT NULL DEFAULT FALSE;
