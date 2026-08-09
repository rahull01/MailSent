# Security Policy

MailSent handles sensitive customer delivery data. Security is treated as a business-trust requirement, per the Security Architecture (`docs/03_Architecture/09_SECURITY_ARCHITECTURE.md`).

## Supported versions

The current `main` branch is supported. Until a release is cut, all of `main` is covered.

## Reporting a vulnerability

- **Do not open a public issue** for a security vulnerability.
- Report privately to the founders/security owner with a clear reproduction and impact.
- Never commit credentials, tokens, or live infrastructure details in any report.

## Project status

- **License:** pending founder decision (see `LICENSE_PENDING.md`).
- **Secrets:** the repository secrets guard (`scripts/guard-secrets.sh`) blocks secret-shaped content; run it before committing.
- **Foundational rules:** truth is non-negotiable (never fabricate a delivery state); no action on customer live infrastructure without explicit Consent; no excess privilege. See the Project Constitution.