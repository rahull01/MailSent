# MailSent — Engineering Foundation (Phase 01) Make targets

.DEFAULT_GOAL := help
.PHONY: help dev build test lint docker-up docker-down

help: ## Show this help and list all available commands
	@echo "MailSent — Engineering targets"
	@echo ""
	@echo "  help         Show this help"
	@echo "  dev          Bring up the local development stack (backend + frontend + infra)"
	@echo "  build        Build backend and frontend"
	@echo "  test         Run the test suites and typecheck"
	@echo "  lint         Lint / format checks and repository guards"
	@echo "  docker-up    Start infrastructure containers"
	@echo "  docker-down  Stop infrastructure containers"
.PHONY: help

dev:
	docker compose up --build

build:
	cd backend && mvn -B -DskipTests package
	cd frontend && npm run build

test:
	cd backend && mvn -B test
	cd frontend && npx tsc --noEmit

lint:
	cd frontend && npm run lint
	cd frontend && npx prettier --check "app/**/*.{ts,tsx}" "components/**/*.{ts,tsx}"
	bash scripts/guard-secrets.sh
	bash scripts/repo-health.sh

docker-up:
	docker compose up -d

docker-down:
	docker compose down