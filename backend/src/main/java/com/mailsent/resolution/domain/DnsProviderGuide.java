package com.mailsent.resolution.domain;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * DnsProviderGuide — Provider-specific instructions for DNS record remediation (INT-002).
 */
public class DnsProviderGuide {

    public enum DnsProvider {
        CLOUDFLARE,
        GODADDY,
        AWS_ROUTE53,
        NAMECHEAP,
        GENERIC
    }

    private final String id;
    private final DnsProvider provider;
    private final String recordType; // TXT, CNAME, MX
    private final String hostName;
    private final String recordValue;
    private final List<String> stepByStepInstructions;

    public DnsProviderGuide(String id, DnsProvider provider, String recordType,
                            String hostName, String recordValue,
                            List<String> stepByStepInstructions) {
        this.id = Objects.requireNonNull(id);
        this.provider = Objects.requireNonNull(provider);
        this.recordType = Objects.requireNonNull(recordType);
        this.hostName = Objects.requireNonNull(hostName);
        this.recordValue = Objects.requireNonNull(recordValue);
        this.stepByStepInstructions = List.copyOf(Objects.requireNonNull(stepByStepInstructions));
    }

    public static DnsProviderGuide generate(DnsProvider provider, String recordType, String hostName, String recordValue) {
        if (recordType.isBlank() || hostName.isBlank() || recordValue.isBlank()) {
            throw new IllegalArgumentException("Record parameters cannot be blank.");
        }

        List<String> steps;
        switch (provider) {
            case CLOUDFLARE:
                steps = List.of(
                        "1. Log in to the Cloudflare dashboard and select your domain.",
                        "2. Navigate to DNS -> Records.",
                        "3. Click 'Add record' and select type '" + recordType + "'.",
                        "4. Set Name to '" + hostName + "' and Target/Content to '" + recordValue + "'.",
                        "5. Ensure Proxy status is set to 'DNS only' (grey cloud), then click Save."
                );
                break;
            case GODADDY:
                steps = List.of(
                        "1. Log in to GoDaddy Domain Control Center.",
                        "2. Select your domain to access Domain Settings, then select Manage DNS.",
                        "3. Select Add and choose Type '" + recordType + "'.",
                        "4. Enter Name '" + hostName + "' and Value '" + recordValue + "'.",
                        "5. Select Save to publish changes."
                );
                break;
            case AWS_ROUTE53:
                steps = List.of(
                        "1. Open the Amazon Route 53 console and navigate to Hosted Zones.",
                        "2. Choose the hosted zone for your domain.",
                        "3. Click 'Create record'.",
                        "4. Enter Record name '" + hostName + "' and select Record type '" + recordType + "'.",
                        "5. In Value, paste '" + recordValue + "' and click Create records."
                );
                break;
            default:
                steps = List.of(
                        "1. Log in to your DNS provider control panel.",
                        "2. Navigate to the DNS Management zone.",
                        "3. Add or update a '" + recordType + "' record.",
                        "4. Set Host/Name to '" + hostName + "' and Value to '" + recordValue + "'.",
                        "5. Save changes and allow TTL propagation."
                );
        }

        return new DnsProviderGuide(UUID.randomUUID().toString(), provider, recordType, hostName, recordValue, steps);
    }

    public String id() { return id; }
    public DnsProvider provider() { return provider; }
    public String recordType() { return recordType; }
    public String hostName() { return hostName; }
    public String recordValue() { return recordValue; }
    public List<String> stepByStepInstructions() { return stepByStepInstructions; }
}
