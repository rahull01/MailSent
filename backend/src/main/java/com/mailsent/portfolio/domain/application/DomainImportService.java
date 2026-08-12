package com.mailsent.portfolio.domain.application;

import java.util.ArrayList;
import java.util.List;

import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.domain.domain.DomainAlreadyExistsException;
import com.mailsent.portfolio.domain.domain.DomainOrganizationNotFoundException;
import com.mailsent.portfolio.organization.application.OrganizationRepository;

/**
 * DomainImportService — the DOM-002 bulk domain import application boundary.
 *
 * <p>Registers many domain names for ONE existing organization in a single operation (FUN-D2:
 * "hold and distinguish many domains for one organization"), reusing {@link DomainService}
 * registration and its invariants: the organization must exist (clear domain error), a domain
 * name is globally unique (existing-domain or within-batch duplicates are reported as
 * failures, never registered twice), names are canonicalized by the existing {@code Domain}
 * contract, and every imported domain is owned by the target organization (INV-09).</p>
 *
 * <p>Import is <em>partial</em> by design: valid names are registered and the rest are
 * reported per-name without aborting the batch. The external CSV file/upload delivery surface
 * is not specified by any frozen contract and is intentionally not implemented here — this
 * operation consumes the domain names directly.</p>
 */
public class DomainImportService {

    private final DomainService domains;
    private final OrganizationRepository organizations;

    public DomainImportService(DomainService domains, OrganizationRepository organizations) {
        this.domains = domains;
        this.organizations = organizations;
    }

    /**
     * Imports a batch of domain names into one existing organization.
     *
     * @param organizationId the owning Organization's stable identity
     * @param domainNames    the offered domain names, in order (may be empty)
     * @return the imported domains plus per-name failures
     * @throws DomainOrganizationNotFoundException if the organization does not exist
     */
    public DomainImportResult importDomains(String organizationId, List<String> domainNames) {
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
}