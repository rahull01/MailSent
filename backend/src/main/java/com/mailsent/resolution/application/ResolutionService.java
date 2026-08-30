package com.mailsent.resolution.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.mailsent.diagnosis.domain.Issue;
import com.mailsent.diagnosis.domain.IssueRepository;
import com.mailsent.diagnosis.domain.RootCause;
import com.mailsent.portfolio.domain.application.DomainRepository;
import com.mailsent.portfolio.domain.domain.Domain;
import com.mailsent.portfolio.observation.domain.HealthIndication;
import com.mailsent.portfolio.observation.domain.HealthSignal;
import com.mailsent.resolution.domain.DnsProvider;
import com.mailsent.resolution.domain.ResolutionAction;

/**
 * ResolutionService — RES-001/002/003 application boundary.
 *
 * <p><strong>Grounding rule (N2 / INV-04).</strong> Every produced step is limited to
 * facts carried by the Issue (domain name, detected root cause, detected sending
 * provider) plus provider-neutral navigation guidance. DNS record <em>names</em> are
 * derived only from the domain fact; DNS record <em>values</em>, selectors, keys, and
 * include mechanisms are NEVER invented — where the evidence does not carry an exact
 * value, {@code recordValue} is {@code null} and the steps direct the user to the
 * authoritative source (their mail/DNS provider). An UNKNOWN health state has no
 * grounded remediation and produces no actions.</p>
 */
@Service
public class ResolutionService {

    private final DomainRepository domainRepository;
    private final IssueRepository issueRepository;

    public ResolutionService(DomainRepository domainRepository, IssueRepository issueRepository) {
        this.domainRepository = domainRepository;
        this.issueRepository = issueRepository;
    }

    public ResolutionService() {
        this(null, null);
    }

    /**
     * Generates tailored resolution actions for a diagnosed Issue based on the DNS provider (RES-003 / FUN-G3).
     */
    public List<ResolutionAction> generateActionsForIssue(String organizationId, String issueId, DnsProvider providerOverride) {
        Objects.requireNonNull(organizationId, "organizationId cannot be null");
        Objects.requireNonNull(issueId, "issueId cannot be null");

        if (issueRepository == null) {
            return List.of();
        }

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + issueId));

        if (!issue.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("Tenant boundary violation: organizationId does not own issue");
        }

        if (domainRepository != null) {
            Domain domain = domainRepository.findById(issue.domainId())
                    .orElseThrow(() -> new IllegalArgumentException("Domain not registered: " + issue.domainId()));
            if (!domain.organizationId().equals(organizationId)) {
                throw new IllegalArgumentException("Tenant boundary violation: organizationId does not own domain");
            }
        }

        DnsProvider provider = providerOverride != null ? providerOverride : DnsProvider.GENERIC_DNS;
        return buildActionsForRootCause(issue, provider);
    }

    private List<ResolutionAction> buildActionsForRootCause(Issue issue, DnsProvider provider) {
        List<ResolutionAction> actions = new ArrayList<>();
        RootCause cause = issue.rootCause();
        String domain = issue.domainName();

        if (cause == RootCause.DMARC_REJECT || cause == RootCause.DMARC_QUARANTINE) {
            List<String> steps = switch (provider) {
                case CLOUDFLARE -> List.of(
                        "Log in to your Cloudflare dashboard and select domain " + domain,
                        "Navigate to DNS -> Records -> Add record",
                        "Select Type: TXT, Name: _dmarc",
                        "Set the value to your chosen DMARC policy record starting with v=DMARC1, keeping your existing rua= reporting address unchanged",
                        "Ensure Proxy status is DNS Only (Grey Cloud), then click Save"
                );
                case ROUTE53 -> List.of(
                        "Open the AWS Route 53 console and select Hosted Zones",
                        "Choose the hosted zone for " + domain,
                        "Click Create record -> Simple routing",
                        "Enter Record name: _dmarc, Record type: TXT",
                        "Set the value to your chosen DMARC policy record starting with v=DMARC1, keeping your existing rua= reporting address unchanged",
                        "Click Create records"
                );
                default -> List.of(
                        "Log in to your DNS provider management console",
                        "Navigate to DNS zone management for " + domain,
                        "Add a new TXT record with Name: _dmarc",
                        "Set the value to your chosen DMARC policy record starting with v=DMARC1, keeping your existing rua= reporting address unchanged",
                        "Save the record and wait for TTL propagation"
                );
            };

            actions.add(ResolutionAction.forIssue(
                    issue.id(),
                    "Publish or correct DMARC policy record",
                    "Configure a valid DMARC TXT record for " + domain + " in " + provider,
                    provider,
                    steps,
                    "TXT",
                    "_dmarc." + domain,
                    null, // exact value is not carried by the evidence; never invented (N2/INV-04)
                    1
            ));
        } else if (cause == RootCause.SPF_ALIGNMENT_FAILURE || cause == RootCause.SPF_AUTHENTICATION_FAILURE) {
            String senderGuidance = issue.sender() != null
                    ? "Add the SPF include mechanism for " + issue.sender().providerName()
                        + " exactly as published in that provider's sender-authorization documentation"
                    : "Identify the sending service from your recent DMARC reports first, then add that provider's SPF include mechanism";
            List<String> steps = switch (provider) {
                case CLOUDFLARE -> List.of(
                        "Log in to Cloudflare and select domain " + domain,
                        "Find your existing TXT record at apex (@) starting with v=spf1",
                        senderGuidance,
                        "Keep TTL set to Auto and Proxy set to DNS Only, then Save"
                );
                case ROUTE53 -> List.of(
                        "Open AWS Route 53 and select hosted zone for " + domain,
                        "Find the apex (@) TXT record containing v=spf1",
                        senderGuidance,
                        "Save record"
                );
                default -> List.of(
                        "Open your DNS management console for " + domain,
                        "Locate your root SPF TXT record (v=spf1 ...)",
                        senderGuidance,
                        "Save changes"
                );
            };

            actions.add(ResolutionAction.forIssue(
                    issue.id(),
                    "Update SPF record for detected sender",
                    "Authorize email sender in the SPF record for " + domain + " in " + provider,
                    provider,
                    steps,
                    "TXT",
                    "@",
                    null, // exact include string must come from the provider's published value; never invented
                    1
            ));
        } else if (cause == RootCause.DKIM_ALIGNMENT_FAILURE || cause == RootCause.DKIM_AUTHENTICATION_FAILURE) {
            List<String> steps = switch (provider) {
                case CLOUDFLARE -> List.of(
                        "Log in to Cloudflare and open DNS settings for " + domain,
                        "In your email provider's admin console, find the DKIM selector name and public key/CNAME target it publishes",
                        "Add a new CNAME or TXT record using that selector under _domainkey, turn off proxying (DNS Only), then Save"
                );
                default -> List.of(
                        "Open your DNS management panel for " + domain,
                        "In your email provider's admin console, find the DKIM selector name and public key/CNAME target it publishes",
                        "Create a new CNAME or TXT record for that selector under _domainkey." + domain + " and Save"
                );
            };

            actions.add(ResolutionAction.forIssue(
                    issue.id(),
                    "Publish DKIM public key record",
                    "Add DKIM signing key to your DNS zone in " + provider,
                    provider,
                    steps,
                    "CNAME/TXT",
                    null, // selector is provider-specific and not carried by the evidence; never invented
                    null, // public key must come from the mail provider; never invented
                    1
            ));
        }

        return actions;
    }

    /**
     * Legacy signal-based action generation (backward compatibility).
     *
     * <p>An UNKNOWN indication carries no grounded remediation (N2): absence of a
     * determinable state must not be answered with invented DNS instructions.</p>
     */
    public List<ResolutionAction> generateActions(HealthSignal signal) {
        if (signal == null) {
            throw new IllegalArgumentException("Health signal is required to generate resolution actions");
        }

        if (signal.indication() == HealthIndication.AT_RISK) {
            return List.of(
                ResolutionAction.create(
                    "Identify failing sender",
                    "Review recent DMARC aggregate reports to pinpoint the source of unaligned emails.",
                    List.of("Open the DMARC reports dashboard for " + signal.domainName(), "Filter by disposition=quarantine or reject"),
                    1
                ),
                ResolutionAction.create(
                    "Review authentication setup",
                    "Ensure SPF and DKIM are valid and correctly aligned.",
                    List.of("Check your DNS provider for TXT records", "Compare the published records with what your mail providers document"),
                    2
                )
            );
        }

        // HEALTHY: nothing to fix. UNKNOWN: no grounded remediation exists yet.
        return List.of();
    }
}
