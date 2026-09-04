-- MYSQL_DATABASE env var already creates cloudopshub_auth on first startup.
-- This script runs alongside it (anything in /docker-entrypoint-initdb.d/ runs
-- automatically, once, only on a brand-new/empty data volume) to also create
-- the database project-service needs.
CREATE DATABASE IF NOT EXISTS cloudopshub_project;