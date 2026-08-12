package com.mailsent.portfolio.domain.application;

/**
 * DomainImportFailure — one domain name that could not be imported (DOM-002 bulk import).
 *
 * @param name   the offered domain name that failed
 * @param reason a concise, business-first reason (e.g. "already registered", "invalid domain name")
 */
public record DomainImportFailure(String name, String reason) {
}