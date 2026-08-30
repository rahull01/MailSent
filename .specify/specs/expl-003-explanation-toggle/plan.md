# Implementation Plan: EXPL-003 — Simpler Toggle

## 1. Architecture & Boundaries
- **Bounded Context**: Business Explanation (`com.mailsent.explanation`).
- **Domain Layer**:
  - `ExplanationMode` enum (`BUSINESS_SUMMARY`, `TECHNICAL_DEEP_DIVE`).
  - `ExplanationView` projection model.
  - Enhanced `Explanation.java` with view rendering and Issue translation support.
- **Application Layer**:
  - `ExplanationService.java` updated with tenant isolation checks (`INV-09`) and mode-based query support.

---

## 2. Package Layout

```
backend/src/main/java/com/mailsent/explanation/
├── domain/
│   ├── Explanation.java       <-- [UPDATED WITH VIEW RENDERING]
│   ├── Grounding.java         <-- [GROUNDING PRESERVED]
│   ├── ExplanationMode.java   <-- [NEW ENUM]
│   └── ExplanationView.java   <-- [NEW PROJECTION]
└── application/
    └── ExplanationService.java <-- [UPDATED WITH MODE & TENANT CHECKS]
```

---

## 3. Testing & Verification Strategy

### Unit Tests:
- `ExplanationViewTest.java`: Verify rendering across all `ExplanationMode` values and health states.
- `ExplanationServiceTest.java`: Verify tenant isolation, domain validation, mode switching, and backward compatibility.
- Hermetic and live PostgreSQL platform regression verification.
