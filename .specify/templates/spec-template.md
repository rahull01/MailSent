# Feature Specification: [FEATURE NAME]

<!--
  AUTHORITY & SUBORDINATION:
  This Feature Specification is a scoped operational slice subordinate to MailSent's authoritative documents:
  - Operational & Product Source of Truth: docs/00_Foundation/MAILSENT_MASTER_WORKFLOW.md
  - Engineering Truth: docs/00_Foundation/PROJECT_CONSTITUTION.md
  - Requirements Baseline: docs/02_Product_Design/ (PRODUCT_REQUIREMENTS.md, FUNCTIONAL_REQUIREMENTS.md)
-->

**Feature Branch**: `[###-feature-name]`  
**Created**: [DATE]  
**Status**: Draft  
**Input**: User description: "$ARGUMENTS"  

---

## 1. Governance & Traceability Anchors *(mandatory)*

- **Master Workflow Module**: [e.g., WF-2 Infrastructure Intelligence, WF-3 Alignment Intelligence, WF-5 Infrastructure Map]
- **Parent Requirement**: [e.g., FUN-H2, FUN-X1, FUN-M4, AIR-AI1, NFR-S1]
- **Customer Problem (CP)**: [e.g., CP-1, CP-2, CP-6 from docs/01_Product/CUSTOMER_PROBLEMS.md]
- **Customer Journey (J) & Value Prop (VP)**: [e.g., J-3, VP-1 from docs/01_Product/]
- **Master Checklist Task ID**: [e.g., MAP-002, DIAG-002, COMP-001 from implementation/00_IMPLEMENTATION_MASTER_CHECKLIST.md]
- **The Golden Rule & Recurring Value**: [Explicit statement of why an agency/customer will pay a monthly subscription for this capability for 12+ months. If NO, reject immediately.]
- **Bounded Context**: [e.g., Delivery Observation, Diagnosis, Business Explanation, Portfolio from docs/03_Architecture/03_BOUNDED_CONTEXTS.md]

---

## 2. User Scenarios & Acceptance Criteria *(mandatory)*

<!--
  User stories MUST be prioritized (P0, P1, P2) and independently testable.
  P0 = Core critical path; P1 = Secondary; P2 = Polish/Advanced.
-->

### User Story 1 - [Brief Title] (Priority: P0 / P1)

[Describe this user journey in plain business language]

- **Why this priority**: [Explain the customer impact and why it holds this priority]
- **Independent Test**: [Describe how this story is verified independently]
- **Acceptance Scenarios**:
  1. **Given** [initial state / observed evidence], **When** [action / event occurs], **Then** [expected truthful outcome]
  2. **Given** [failure or missing data state], **When** [action occurs], **Then** [system degrades gracefully without fabrication (N2, N3)]

---

### User Story 2 - [Brief Title] (Priority: P1 / P2)

[Describe this user journey in plain business language]

- **Why this priority**: [Value rationale]
- **Independent Test**: [Independent verification approach]
- **Acceptance Scenarios**:
  1. **Given** [initial state], **When** [action], **Then** [expected outcome]

---

## 3. UX Standard — The 5 Questions *(mandatory for UI/Alert features)*

If this feature exposes information to users (UI, dashboard, reports, alerts), answer all 5 questions in order:
1. **What happened?** [Clear, plain-language description of state/event]
2. **Why?** [Underlying root cause, understandable by non-technical user]
3. **What is the business impact?** [Revenue, delivery, or reputation risk]
4. **What should the user do?** [Concrete, prioritized next step]
5. **What happens if ignored?** [Honest consequence of inaction]

---

## 4. Requirements *(mandatory)*

<!--
  Requirements must adhere to Ubiquitous Language (docs/00_Foundation/GLOSSARY.md)
  and cite parent functional requirements.
-->

### Functional Requirements
- **FR-001**: System MUST [specific capability referencing domain entities from GLOSSARY.md]
- **FR-002**: System MUST [data validation or mutation behavior]
- **FR-003**: System MUST preserve deterministic lineage (`INV-11`) linking outputs to source evidence IDs.
- **FR-004**: System MUST enforce multi-tenant isolation (`INV-09`) scoped to `organization_id`.

### Edge Cases & Failure Paths
- What happens when evidence/observation is unavailable? (Rule: Persist nothing or report unavailable; NEVER guess or fabricate - N2).
- How does the system handle transient provider timeouts or malformed inputs? (Rule: Fail loudly, log with MDC, handle gracefully - N3).

---

## 5. Non-Negotiable Compliance Gate *(mandatory)*

- [ ] **N1 (Validated)**: Solves a validated customer problem with 12+ month recurring value.
- [ ] **N2 (No Guessing)**: No fabricated health states, simulated scores, or ungrounded conclusions.
- [ ] **N3 (No Silent Loss)**: All error states and failures are logged and surfaced.
- [ ] **N4 (No Secrets)**: Zero credentials, tokens, or PII exposed in code, logs, or fixtures.
- [ ] **N6 (Honesty)**: All UI copy and error messages describe reality truthfully.

---

## 6. Assumptions & Scope Boundaries

- [Assumption about prerequisite data or prior checklist phase completion]
- **Out of Scope (V1 Exclusions)**: [e.g., No auto-DNS modification, no fake AI, no deliverability simulation]
