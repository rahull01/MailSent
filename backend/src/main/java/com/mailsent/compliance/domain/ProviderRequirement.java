package com.mailsent.compliance.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * ProviderRequirement — Individual requirement evaluated against mailbox provider criteria (FUN-C1 / FUN-C2).
 */
public class ProviderRequirement {

    private final String id;
    private final String name;
    private final String description;
    private final String businessMeaning;
    private final RequirementStatus status;
    private final String evidenceRef;

    public ProviderRequirement(String id, String name, String description,
                               String businessMeaning, RequirementStatus status, String evidenceRef) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.businessMeaning = Objects.requireNonNull(businessMeaning);
        this.status = Objects.requireNonNull(status);
        this.evidenceRef = evidenceRef;
    }

    public static ProviderRequirement of(String name, String description,
                                         String businessMeaning, RequirementStatus status, String evidenceRef) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Requirement name must not be blank.");
        }
        return new ProviderRequirement(UUID.randomUUID().toString(), name, description, businessMeaning, status, evidenceRef);
    }

    public String id() { return id; }
    public String name() { return name; }
    public String description() { return description; }
    public String businessMeaning() { return businessMeaning; }
    public RequirementStatus status() { return status; }
    public String evidenceRef() { return evidenceRef; }
}
