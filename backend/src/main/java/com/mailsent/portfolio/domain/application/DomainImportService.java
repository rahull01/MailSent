package com.mailsent.portfolio.domain.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mailsent.identity.authentication.domain.User;
import com.mailsent.identity.team.domain.AccessDeniedException;
import com.mailsent.identity.team.application.AuthorizationService;
import com.mailsent.identity.team.domain.Capability;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.domain.DomainAlreadyExistsException;
import com.mailsent.portfolio.domain.domain.DomainOrganizationNotFoundException;
import com.mailsent.portfolio.organization.application.OrganizationRepository;

/**
 * DomainImportService — the DOM-002 bulk domain import application boundary.
 *
 * <p>Registers many domain names for ONE existing organization on behalf of an
 * <strong>authenticated</strong> caller (FUN-D2: "hold and distinguish many domains for one
 * organization"), reusing {@link DomainService} registration and its invariants: the
 * organization must exist (clear domain error), a domain name is globally unique
 * (existing-domain or within-batch duplicates are reported as failures, never registered
 * twice), names are canonicalized by the existing {@code Domain} contract, and every
 * imported domain is owned by the target organization (INV-09).</p>
 *
 * <p><strong>Authorization.</strong> Mirrors DOM-001 exactly (SEC §4 ownership,
 * NFR-S1 least privilege): the caller's home organization must equal the target
 * organization (INV-09), and the caller must hold {@link Capability#DOMAIN_MANAGE}
 * (Role.OWNER). There is no unauthenticated path.</p>
 *
 * <p>Import is <em>partial</em> by design: valid names are registered and the rest are
 * reported per-name without aborting the batch. The external CSV file/upload delivery surface
 * is not specified by any frozen contract and is intentionally not implemented here — this
 * operation consumes the domain names directly.</p>
 */
public class DomainImportService {

    private final DomainService domains;
    private final OrganizationRepository organizations;
    private final AuthorizationService authorizationService;

    public DomainImportService(DomainService domains, OrganizationRepository organizations,
                               AuthorizationService authorizationService) {
        this.domains = Objects.requireNonNull(domains, "DomainService is required");
        this.organizations = organizations;
        this.authorizationService = Objects.requireNonNull(authorizationService, "AuthorizationService is required");
    }

    /**
     * Imports a batch of domain names into one existing organization on behalf of an
     * authenticated caller authorized for {@link Capability#DOMAIN_MANAGE} in that
     * organization.
     *
     * @param caller         the authenticated user performing the import
     * @param organizationId the owning Organization's stable identity
     * @param domainNames    the offered domain names, in order (may be empty)
     * @return the imported domains plus per-name failures
     * @throws AccessDeniedException                if the caller is unauthenticated, crosses a
     *                                              tenant boundary (INV-09), or lacks DOMAIN_MANAGE
     * @throws DomainOrganizationNotFoundException if the organization does not exist
     */
    public DomainImportResult importDomains(User caller, String organizationId, List<String> domainNames) {
        if (caller == null) {
            throw new AccessDeniedException("anonymous", organizationId, Capability.DOMAIN_MANAGE,
                "Caller is unauthenticated.");
        }
        if (!authorizationService.isAuthorized(caller, organizationId, Capability.DOMAIN_MANAGE)) {
            throw new AccessDeniedException(caller.id(), organizationId, Capability.DOMAIN_MANAGE,
                String.format("Caller '%s' is not authorized to manage domains of organization '%s'",
                    caller.id(), organizationId));
        }

        organizations.findById(organizationId)
            .orElseThrow(() -> new DomainOrganizationNotFoundException(organizationId));

        List<Domain> imported = new ArrayList<>();
        List<DomainImportFailure> failures = new ArrayList<>();
        if (domainNames != null) {
            for (String name : domainNames) {
                try {
                    imported.add(domains.register(organizationId, name));
                } catch (DomainAlreadyExistsException ex) {
                    failures.add(new DomainImportFailure(name, "already registered"));
                } catch (IllegalArgumentException ex) {
                    failures.add(new DomainImportFailure(name, "invalid domain name"));
                }
            }
        }
        return new DomainImportResult(imported, failures);
    }

    /**
     * Imports domains from bounded UTF-8 CSV bytes on behalf of an authenticated caller
     * (DOM-002 frozen executable contract).
     *
     * <p>Authorization runs identically to {@link #importDomains(User, String, List)}
     * and strictly BEFORE any parsing or organization-specific work. The CSV grammar,
     * header rule, duplicate policies, per-row partial-success semantics and resource
     * limits are exactly the frozen contract implemented by {@link DomainCsvParser}
     * plus the row pipeline below:</p>
     *
     * <ol>
     *   <li>parse (file-level rejections abort before any registration)</li>
     *   <li>header consumption: first record equal (ignore-case) to {@code domain}</li>
     *   <li>per-row: trim → empty ⇒ "domain name is required"; &gt;253 chars ⇒
     *       "field too long" (parser already bounds); normalized in-file duplicate ⇒
     *       "duplicate in file"</li>
     *   <li>registration via the existing per-row transaction boundary;
     *       uniqueness constraint ⇒ "already registered"</li>
     * </ol>
     *
     * @param caller         the authenticated user performing the import
     * @param organizationId the owning Organization's stable identity
     * @param csvBytes       the raw UTF-8 CSV bytes (BOM tolerated)
     * @return imported domains plus failures, in encounter order
     * @throws AccessDeniedException                unauthenticated / cross-tenant / lacks DOMAIN_MANAGE
     * @throws DomainOrganizationNotFoundException  unknown target organization
     * @throws DomainCsvFileException               INPUT_TOO_LARGE / INVALID_ENCODING / TOO_MANY_RECORDS
     */
    public DomainImportResult importDomainsFromCsv(User caller, String organizationId, byte[] csvBytes) {
        if (caller == null) {
            throw new AccessDeniedException("anonymous", organizationId, Capability.DOMAIN_MANAGE,
                "Caller is unauthenticated.");
        }
        if (!authorizationService.isAuthorized(caller, organizationId, Capability.DOMAIN_MANAGE)) {
            throw new AccessDeniedException(caller.id(), organizationId, Capability.DOMAIN_MANAGE,
                String.format("Caller '%s' is not authorized to manage domains of organization '%s'",
                    caller.id(), organizationId));
        }
        organizations.findById(organizationId)
            .orElseThrow(() -> new DomainOrganizationNotFoundException(organizationId));

        List<DomainCsvParser.Row> rows = DomainCsvParser.parse(csvBytes);

        List<Domain> imported = new ArrayList<>();
        List<DomainImportFailure> failures = new ArrayList<>();
        java.util.Set<String> seenNormalized = new java.util.HashSet<>();
        boolean headerChecked = false;

        for (DomainCsvParser.Row row : rows) {
            switch (row.kind()) {
                case MULTIPLE_COLUMNS ->
                    failures.add(new DomainImportFailure("", "multiple columns; one domain per line"));
                case MALFORMED_QUOTING ->
                    failures.add(new DomainImportFailure("", "malformed CSV row"));
                case FIELD_TOO_LONG ->
                    failures.add(new DomainImportFailure(row.value(), "field too long"));
                case DOMAIN -> {
                    String trimmed = row.value().trim();
                    if (!headerChecked && "domain".equalsIgnoreCase(trimmed)) {
                        headerChecked = true;
                        continue; // optional frozen header — never registered
                    }
                    headerChecked = true;
                    if (trimmed.isEmpty()) {
                        failures.add(new DomainImportFailure(trimmed, "domain name is required"));
                        continue;
                    }
                    String normalized = Domain.normalize(trimmed);
                    if (!seenNormalized.add(normalized)) {
                        failures.add(new DomainImportFailure(trimmed, "duplicate in file"));
                        continue;
                    }
                    try {
                        imported.add(domains.register(organizationId, trimmed));
                    } catch (DomainAlreadyExistsException ex) {
                        failures.add(new DomainImportFailure(trimmed, "already registered"));
                    } catch (IllegalArgumentException ex) {
                        failures.add(new DomainImportFailure(trimmed, "invalid domain name"));
                    }
                }
            }
        }
        return new DomainImportResult(imported, failures);
    }
}
