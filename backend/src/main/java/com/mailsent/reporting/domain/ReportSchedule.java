package com.mailsent.reporting.domain;

import java.util.Objects;
import java.util.UUID;

import com.mailsent.alerts.domain.AlertChannel;

/**
 * ReportSchedule — REP-003 Schedule & deliver.
 *
 * <p>Manages the recurring generation and delivery of reports and executive summaries.
 * This directly satisfies the FUN-R1 requirement that summaries be "recurring" by
 * coupling a target (domain/portfolio) with a frequency and a delivery channel.</p>
 */
public class ReportSchedule {

    public enum Frequency {
        WEEKLY,
        MONTHLY
    }

    private final String id;
    private final String targetId;
    private final Frequency frequency;
    private final AlertChannel deliveryChannel;

    private ReportSchedule(String id, String targetId, Frequency frequency, AlertChannel deliveryChannel) {
        this.id = Objects.requireNonNull(id);
        this.targetId = Objects.requireNonNull(targetId);
        this.frequency = Objects.requireNonNull(frequency);
        this.deliveryChannel = Objects.requireNonNull(deliveryChannel);
    }

    /**
     * Configures a new recurring report schedule.
     *
     * @param targetId        the identity of the domain or portfolio to report on
     * @param frequency       how often the report should be generated (e.g. WEEKLY)
     * @param deliveryChannel where the report should be delivered
     * @return a new ReportSchedule instance
     */
    public static ReportSchedule configure(String targetId, Frequency frequency, AlertChannel deliveryChannel) {
        return new ReportSchedule(UUID.randomUUID().toString(), targetId, frequency, deliveryChannel);
    }

    public String id() { return id; }
    public String targetId() { return targetId; }
    public Frequency frequency() { return frequency; }
    public AlertChannel deliveryChannel() { return deliveryChannel; }
}
