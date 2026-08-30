package com.mailsent.diagnosis.domain;

import java.util.List;
import java.util.Optional;

public interface IssueRepository {

    Issue save(Issue issue);

    Optional<Issue> findById(String id);

    List<Issue> findByOrganizationId(String organizationId);

    List<Issue> findByOrganizationIdAndDomainId(String organizationId, String domainId);

    Optional<Issue> findActiveIssue(String organizationId, String domainId, RootCause rootCause, String providerId);
}
