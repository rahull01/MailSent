# Contributing to MailSent

This guide governs how to contribute to the repository. It derives from the Project Constitution and the implementation operating manual (`implementation/README.md`). Read both before contributing.

## Ground rules

- **Implementation follows documentation.** The approved documentation in `docs/` is frozen and is the single source of truth. Code follows architecture; architecture follows requirements; requirements follow customer value.
- **No undocumented work.** Build only what is defined in the implementation Master Checklist. If a feature is not there, it does not get built.
- **No feature creep.** Implement the documented feature and nothing extra.
- **Report conflicts.** If implementation and documentation appear to conflict, stop and report; never resolve a contradiction by editing the source or by assumption.

## Workflow

1. Read the relevant implementation phase and its governing documentation before writing code.
2. Work on a branch; keep changes small and logically atomic.
3. Implement within the architecture and the language of the Glossary.
4. Write and run tests, including failure paths.
5. Verify traceability: the change traces to a requirement, an architecture element, and a customer problem.
6. Submit for review (code, architecture, founder) before merging.
7. Update the checklist status and progress after completion.
8. Never commit secrets. The secrets guard (`scripts/guard-secrets.sh`) blocks secret-shaped content; run it before committing.

## Definition of done

A change is complete only when **all** pass: code compiles, tests pass, review passed, documentation updated, traceability holds, acceptance criteria met, and deployment is ready. One false condition means the work is not done.

## Repository hygiene

- Keep the `main` branch shippable.
- Do not force-push shared history.
- Never modify `docs/` on your own; any required documentation change goes through the founders and governance.
