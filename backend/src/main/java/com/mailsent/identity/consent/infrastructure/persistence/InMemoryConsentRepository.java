package com.mailsent.identity.consent.infrastructure.persistence;

import com.mailsent.identity.consent.application.ConsentRepository;
import com.mailsent.identity.consent.domain.ActionType;
import com.mailsent.identity.consent.domain.Consent;
import com.mailsent.identity.consent.domain.Status;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("test")
public class InMemoryConsentRepository implements ConsentRepository {

    private final Map<String, Consent> data = new ConcurrentHashMap<>();

    @Override
    public void save(Consent consent) {
        data.put(consent.getId(), consent);
    }

    @Override
    public Optional<Consent> findById(String id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public Optional<Consent> findByDomainIdAndActionTypeAndStatus(String domainId, ActionType actionType, Status status) {
        return data.values().stream()
            .filter(c -> c.getDomainId().equals(domainId) && c.getActionType() == actionType && c.getStatus() == status)
            .findFirst();
    }
}
