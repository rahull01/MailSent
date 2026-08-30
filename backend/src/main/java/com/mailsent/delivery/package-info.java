/**
 * Delivery Observation bounded context.
 * Reads a domain's delivery infrastructure into Observation Evidence,
 * Reverse DNS Observation, SMTP Greeting Observation, DNS Health Observation,
 * Reputation Observation, and Blacklist Observation (immutable snapshots of
 * observed facts); persists DMARC Report Receipt from RUA ingest (INT-003);
 * and later produces the Health Signal. These snapshots are not
 * customer-facing proof of health. Reputation Observation is not a Reputation
 * aggregate. Reverse DNS Observation is not FCrDNS, not Health Signal, and not
 * Observation Evidence (DNS remains MX, TXT, SPF). SMTP Greeting Observation is
 * not HELO/EHLO validation, not Health Signal, and not Observation Evidence
 * (greeting/EHLO is a separate snapshot). DNS Health Observation is not Health
 * Signal, not Trust Score, not HEALTH-001, and not Observation Evidence
 * (TTL/DNSSEC/propagation are a separate snapshot). Blacklist Observation is
 * not Health Signal and not a listed/not-listed conclusion. INF-004 does not
 * yet re-observe DNS Health Observation or Blacklist Observation.
 * See 03_BOUNDED_CONTEXTS.md.
 */
package com.mailsent.delivery;
