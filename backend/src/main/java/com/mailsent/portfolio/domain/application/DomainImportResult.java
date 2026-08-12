package com.mailsent.portfolio.domain.application;

import java.util.List;

import com.mailsent.portfolio.domain.domain.Domain;

/**
 * DomainImportResult — the outcome of a bulk domain import (DOM-002).
 *
 * <p>Bulk import is intentionally <em>partial</em>: each offered name is either imported
 * (present in {@link #imported}, ordered as offered) or reported in {@link #failures}
 * without aborting the batch. Duplicate (global-unique) and invalid names are failures; the
 * rest are registered. Imported domains belong to exactly the target organization (INV-09).</p>
 *
 * @param imported the domains successfully registered (order preserved)
 * @param failures the offered names that were not registered, with reasons
 */
public record DomainImportResult(List<Domain> imported, List<DomainImportFailure> failures) {
}