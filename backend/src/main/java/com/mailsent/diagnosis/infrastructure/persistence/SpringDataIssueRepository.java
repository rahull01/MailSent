package com.mailsent.diagnosis.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataIssueRepository extends JpaRepository<IssueJpaEntity, String> {

    List<IssueJpaEntity> findByOrganizationId(String organizationId);

    List<IssueJpaEntity> findByOrganizationIdAndDomainId(String organizationId, String domainId);

    @Query("SELECT i FROM IssueJpaEntity i WHERE i.organizationId = :orgId AND i.domainId = :domainId AND i.rootCause = :rootCause AND ((:providerId IS NULL AND i.providerId IS NULL) OR i.providerId = :providerId) AND i.status IN ('DISCOVERED', 'DIAGNOSED', 'RESOLVED')")
    Optional<IssueJpaEntity> findActiveIssue(@Param("orgId") String orgId,
                                             @Param("domainId") String domainId,
                                             @Param("rootCause") String rootCause,
                                             @Param("providerId") String providerId);
}
