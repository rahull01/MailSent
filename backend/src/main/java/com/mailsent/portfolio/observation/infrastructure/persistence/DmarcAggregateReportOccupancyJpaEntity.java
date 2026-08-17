package com.mailsent.portfolio.observation.infrastructure.persistence;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * Occupancy row for one registered Domain + {@code report_id}. Persistence
 * only — not a public domain concept. Unique on (domain_id, report_id).
 */
@Entity
@Table(name = "dmarc_aggregate_report_occupancy")
@IdClass(DmarcAggregateReportOccupancyJpaEntity.Key.class)
public class DmarcAggregateReportOccupancyJpaEntity {

    @Id
    @Column(name = "domain_id", length = 36, nullable = false)
    private String domainId;

    @Id
    @Column(name = "report_id", nullable = false, columnDefinition = "TEXT")
    private String reportId;

    @Column(name = "organization_id", length = 36, nullable = false)
    private String organizationId;

    protected DmarcAggregateReportOccupancyJpaEntity() {
    }

    public DmarcAggregateReportOccupancyJpaEntity(String domainId, String reportId, String organizationId) {
        this.domainId = domainId;
        this.reportId = reportId;
        this.organizationId = organizationId;
    }

    public String domainId() {
        return domainId;
    }

    public String reportId() {
        return reportId;
    }

    public String organizationId() {
        return organizationId;
    }

    public static final class Key implements Serializable {

        private String domainId;
        private String reportId;

        public Key() {
        }

        public Key(String domainId, String reportId) {
            this.domainId = domainId;
            this.reportId = reportId;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key key)) {
                return false;
            }
            return Objects.equals(domainId, key.domainId) && Objects.equals(reportId, key.reportId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(domainId, reportId);
        }
    }
}
