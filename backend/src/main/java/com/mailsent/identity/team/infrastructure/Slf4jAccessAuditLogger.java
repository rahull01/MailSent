package com.mailsent.identity.team.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.mailsent.identity.team.domain.AccessAuditRecord;
import com.mailsent.identity.team.domain.AccessAuditSink;
import com.mailsent.identity.team.domain.AccessDecision;

/**
 * Slf4jAccessAuditLogger — Default logging adapter implementing the AccessAuditSink port (TEAM-003).
 *
 * <p>Emits structured audit log entries with MDC diagnostic context (userId, userOrg, targetOrg, capability, decision).
 * Does not perform database operations; fast and hermetic.</p>
 */
@Component
public class Slf4jAccessAuditLogger implements AccessAuditSink {

    private static final Logger log = LoggerFactory.getLogger(Slf4jAccessAuditLogger.class);

    @Override
    public void record(AccessAuditRecord record) {
        if (record == null) {
            return;
        }

        String prevUser = MDC.get("audit.userId");
        String prevUserOrg = MDC.get("audit.userOrg");
        String prevTargetOrg = MDC.get("audit.targetOrg");
        String prevCapability = MDC.get("audit.capability");
        String prevDecision = MDC.get("audit.decision");

        try {
            MDC.put("audit.userId", record.userId());
            MDC.put("audit.userOrg", record.userOrganizationId());
            MDC.put("audit.targetOrg", record.targetOrganizationId());
            MDC.put("audit.capability", record.capability() != null ? record.capability().name() : "none");
            MDC.put("audit.decision", record.decision().name());

            if (record.decision() == AccessDecision.DENIED) {
                log.warn("ACCESS_DENIED: actor={}, userOrg={}, targetOrg={}, capability={}, reason={}",
                        record.userId(), record.userOrganizationId(), record.targetOrganizationId(),
                        record.capability(), record.reason());
            } else {
                log.info("ACCESS_GRANTED: actor={}, userOrg={}, targetOrg={}, capability={}",
                        record.userId(), record.userOrganizationId(), record.targetOrganizationId(),
                        record.capability());
            }
        } finally {
            restoreOrRemove("audit.userId", prevUser);
            restoreOrRemove("audit.userOrg", prevUserOrg);
            restoreOrRemove("audit.targetOrg", prevTargetOrg);
            restoreOrRemove("audit.capability", prevCapability);
            restoreOrRemove("audit.decision", prevDecision);
        }
    }

    private void restoreOrRemove(String key, String previousValue) {
        if (previousValue != null) {
            MDC.put(key, previousValue);
        } else {
            MDC.remove(key);
        }
    }
}
