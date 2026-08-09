package com.mailsent.identity.authentication.domain;

/**
 * SessionToken — the credential that authenticates a {@link Session}.
 *
 * <p>A session has a transient <em>raw token</em> (shown to the caller exactly once,
 * used at sign-in to prove the session) and a stored <em>token hash</em>. Only the hash
 * is ever persisted, so a database leak never exposes live bearer tokens. The raw value
 * is generated with high entropy by a {@link SessionTokenFactory} and is never logged or
 * stored.</p>
 *
 * @param rawToken the value returned to the caller (shown once, never stored)
 * @param tokenHash the irreversible stored representation used to authenticate the session
 */
public record SessionToken(String rawToken, String tokenHash) {
}