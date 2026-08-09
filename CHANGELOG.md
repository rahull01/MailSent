# Changelog

All notable changes to this repository are recorded here, in the spirit of the Documentation Standards in the Project Constitution: documentation stays current, and the record is honest. This file is maintained with each merged change.

The format is based on [Keep a Changelog](https://keepachangelog.com/) and this project adheres to [Semantic Versioning](https://semver.org/) once a release version exists.

## [Unreleased]

### Added
- **Repository Foundation (Phase 00):**
  - Root repository `README.md` establishing the product identity and the frozen `docs/` as the single source of truth.
  - `CONTRIBUTING.md` governing how to contribute (branch, review, testing, traceability, no undocumented work).
  - `CHANGELOG.md` (this file) establishing the change-record discipline.
  - `.gitignore` covering secrets, credentials, OS, IDE, and build artifacts.
  - `scripts/guard-secrets.sh` — the repository secrets guard that blocks secret-shaped content from entering the repository (NFR S5). Extended (REPO-005) to detect `*.secret`, `*.token`, `*.p12`, `*.pfx`, `*.jks`, `*.keystore`, service-account keys, SSH/private keys, and GitHub/OpenAI/Anthropic/Google/AWS/Azure credentials.
  - `scripts/repo-health.sh` — the repository health check (REPO-004): structure, naming, required files, line endings, executable scripts, duplicate files, empty directories, broken links, invalid filenames.
  - `implementation/05_REPO_006_VALIDATION_REPORT.md` — the Phase 00 validation report (REPO-006).
  - `.editorconfig` — repository-wide standards (REPO-007): UTF-8, LF, final newline, indentation, trim trailing whitespace; no language-specific formatting.
  - `LICENSE_PENDING.md` — license status pending founder decision (REPO-008); no OSS license chosen, founders own copyright, no contributor may assume licensing.

- **Engineering Foundation (Phase 01):**
  - Backend foundation (`backend/`): Spring Boot 3 / Java 21, Maven build, structured error handling (`GlobalExceptionHandler`), request-id logging (`RequestIdFilter`), validation, JPA (PostgreSQL), Redis, Spring Actuator observability, hermetic test profile + MVC tests.
  - Frontend foundation (`frontend/`): Next.js / TypeScript / Tailwind baseline app shell with lint + build scripts.
  - Infrastructure foundation: `docker-compose.yml` (PostgreSQL, Redis, backend, frontend), `.env.example`, per-service `Dockerfile`s.
  - CI foundation: `.github/workflows/ci.yml` (secrets guard, repository health, `mvn verify`, `npm lint` + `npm build`).
  - Tech stack locked by CTO; Maven selected as the build tool (neither Maven nor Gradle was standardized in the approved documentation).
