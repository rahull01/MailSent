package com.mailsent.portfolio.observation.domain;

import java.util.List;

/**
 * ParsedDmarcRecord — one raw RFC 7489 {@code record} fact set (INT-004).
 *
 * <p>These are extracted XML facts, not Alignment and not Health Signal.
 * Absent nodes stay {@code null} (or an empty {@code dkim_domains} list).
 * {@code spf_aligned}/{@code dkim_aligned} are the raw
 * {@code policy_evaluated/spf} and {@code policy_evaluated/dkim} values.
 * {@code disposition} is raw {@code policy_evaluated/disposition}.
 * {@code spf_result}/{@code dkim_result} are raw {@code auth_results} results.</p>
 */
public record ParsedDmarcRecord(
        String sourceIp,
        Long count,
        String spfResult,
        String dkimResult,
        String spfAligned,
        String dkimAligned,
        String envelopeFrom,
        String headerFrom,
        List<String> dkimDomains,
        String disposition) {

    public ParsedDmarcRecord {
        dkimDomains = dkimDomains == null ? List.of() : List.copyOf(dkimDomains);
    }
}
