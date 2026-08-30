package com.mailsent.resolution.domain;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * ResolutionAction — actionable, step-by-step guidance tailored to the DNS provider (RES-001/002/003).
 */
public class ResolutionAction {

    private final String id;
    private final String issueId; // Lineage to Issue (nullable for legacy signal actions)
    private final String title;
    private final String description;
    private final DnsProvider dnsProvider;
    private final List<String> exactSteps;
    private final String recordType; // e.g. "TXT", "CNAME"
    private final String recordName; // e.g. "_dmarc.example.com"
    private final String recordValue; // exact DNS value
    private final int priority;

    public ResolutionAction(String id, String issueId, String title, String description,
                            DnsProvider dnsProvider, List<String> exactSteps,
                            String recordType, String recordName, String recordValue, int priority) {
        this.id = Objects.requireNonNull(id, "ID is required");
        this.issueId = issueId;
        this.title = Objects.requireNonNull(title, "Title is required");
        this.description = Objects.requireNonNull(description, "Description is required");
        this.dnsProvider = Objects.requireNonNull(dnsProvider, "DnsProvider is required");
        this.exactSteps = List.copyOf(Objects.requireNonNull(exactSteps, "Exact steps are required"));
        this.recordType = recordType;
        this.recordName = recordName;
        this.recordValue = recordValue;
        this.priority = priority;
    }

    public static ResolutionAction create(String title, String description, List<String> exactSteps, int priority) {
        return new ResolutionAction(UUID.randomUUID().toString(), null, title, description,
                DnsProvider.GENERIC_DNS, exactSteps, null, null, null, priority);
    }

    public static ResolutionAction forIssue(String issueId, String title, String description,
                                            DnsProvider dnsProvider, List<String> exactSteps,
                                            String recordType, String recordName, String recordValue, int priority) {
        return new ResolutionAction(UUID.randomUUID().toString(), issueId, title, description,
                dnsProvider, exactSteps, recordType, recordName, recordValue, priority);
    }

    public String id() { return id; }
    public String issueId() { return issueId; }
    public String title() { return title; }
    public String description() { return description; }
    public DnsProvider dnsProvider() { return dnsProvider; }
    public List<String> exactSteps() { return exactSteps; }
    public String recordType() { return recordType; }
    public String recordName() { return recordName; }
    public String recordValue() { return recordValue; }
    public int priority() { return priority; }
}
