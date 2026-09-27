\set ON_ERROR_STOP on

-- Usuário e banco do Workflow
CREATE ROLE workflow_app
    LOGIN
  NOSUPERUSER
  NOCREATEDB
  NOCREATEROLE;

CREATE DATABASE workflow OWNER workflow_app;

REVOKE ALL ON DATABASE workflow FROM PUBLIC;

-- Usuário e banco do Integration
CREATE ROLE integration_app
    LOGIN
  NOSUPERUSER
  NOCREATEDB
  NOCREATEROLE;

CREATE DATABASE integration OWNER integration_app;

REVOKE ALL ON DATABASE integration FROM PUBLIC;