package com.mailsent.identity.consent.application;

import com.mailsent.identity.consent.domain.ActionType;
import com.mailsent.identity.consent.domain.Consent;
import com.mailsent.identity.consent.domain.Status;
import java.util.Optional;

public interface ConsentRepository {
    void save(Consent consent);
    Optional<Consent> findById(String id);
    Optional<Consent> findByDomainIdAndActionTypeAndStatus(String domainId, ActionType actionType, Status status);
}
