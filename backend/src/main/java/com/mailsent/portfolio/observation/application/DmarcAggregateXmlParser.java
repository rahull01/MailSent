package com.mailsent.portfolio.observation.application;

/**
 * Reads opaque RUA bytes (XML, or zip/gz wrapping XML) into RFC 7489
 * aggregate facts. Does not know tenant identity and does not persist.
 */
public interface DmarcAggregateXmlParser {

    DmarcAggregateXmlExtraction extract(byte[] payload);
}
