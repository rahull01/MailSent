package com.mailsent.portfolio.observation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for Domain + {@code report_id} occupancy. Insert-only
 * at the application port; uniqueness is the composite primary key.
 */
public interface JpaDmarcAggregateReportOccupancyRepository
    extends JpaRepository<DmarcAggregateReportOccupancyJpaEntity, DmarcAggregateReportOccupancyJpaEntity.Key> {
}
