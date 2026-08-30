package com.mailsent.diagnosis.application;

import com.mailsent.diagnosis.domain.Sender;
import com.mailsent.portfolio.observation.domain.DmarcAggregateEvidence;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * SenderDetectionService — Implements DIAG-002 (Sender detection).
 * 
 * <p>Analyzes observation evidence to detect recognized sending providers
 * (e.g. Google Workspace, Microsoft 365, Salesforce, Mailchimp).
 * Ensures tenant isolation because it operates strictly on the 
 * evidence context provided for a specific domain.</p>
 */
@Service
public class SenderDetectionService {

    public static final Sender GOOGLE_WORKSPACE = new Sender("google_workspace", "Google Workspace");
    public static final Sender MICROSOFT_365 = new Sender("microsoft_365", "Microsoft 365");
    public static final Sender SALESFORCE = new Sender("salesforce", "Salesforce");
    public static final Sender MAILCHIMP = new Sender("mailchimp", "Mailchimp");

    /**
     * Detects a recognized sender based on the provided DMARC aggregate evidence.
     * 
     * @param evidence The DMARC aggregate evidence snapshot.
     * @return An Optional containing the detected Sender, or empty if unknown.
     */
    public Optional<Sender> detectFromEvidence(DmarcAggregateEvidence evidence) {
        if (evidence == null) {
            return Optional.empty();
        }

        // 1. Detect from DKIM domains
        for (String dkim : evidence.dkimDomains()) {
            String lowerDkim = dkim.toLowerCase();
            if (lowerDkim.endsWith("google.com")) {
                return Optional.of(GOOGLE_WORKSPACE);
            }
            if (lowerDkim.endsWith("outlook.com") || lowerDkim.endsWith("onmicrosoft.com") || lowerDkim.endsWith("microsoft.com")) {
                return Optional.of(MICROSOFT_365);
            }
            if (lowerDkim.endsWith("salesforce.com") || lowerDkim.endsWith("sfdc.net")) {
                return Optional.of(SALESFORCE);
            }
            if (lowerDkim.endsWith("rsgsv.net") || lowerDkim.endsWith("mcsv.net") || lowerDkim.endsWith("mailchimp.com")) {
                return Optional.of(MAILCHIMP);
            }
        }

        // 2. Detect from Envelope From
        if (evidence.envelopeFrom() != null) {
            String ef = evidence.envelopeFrom().toLowerCase();
            if (ef.contains("salesforce.com") || ef.contains("sfdc.net")) {
                return Optional.of(SALESFORCE);
            }
            if (ef.contains("rsgsv.net") || ef.contains("mcsv.net") || ef.contains("mailchimp.com")) {
                return Optional.of(MAILCHIMP);
            }
            if (ef.contains("google.com")) {
                return Optional.of(GOOGLE_WORKSPACE);
            }
            if (ef.contains("outlook.com") || ef.contains("onmicrosoft.com")) {
                return Optional.of(MICROSOFT_365);
            }
        }

        // 3. Detect from Header From (less reliable, but possible fallback if aligned)
        // Usually, envelope from and DKIM are the best indicators of the actual sender infrastructure.

        return Optional.empty();
    }
}
