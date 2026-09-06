package com.mailsent.portfolio.domain.application;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * DomainCsvParser — the DOM-002 bounded CSV grammar layer (frozen executable contract).
 *
 * <p>Pure, deterministic and side-effect free: no database, no network, no logging, no
 * {@code DomainService} access. It enforces ONLY the frozen CSV-format constraints:</p>
 *
 * <ul>
 *   <li>Input: UTF-8 bytes ≤ {@link #MAX_INPUT_BYTES}; one leading BOM removed;
 *       invalid UTF-8 is a file-level rejection.</li>
 *   <li>Records separated by CRLF / LF / CR; fully blank records skipped.</li>
 *   <li>Exactly one field per record: an unquoted comma ⇒ MULTIPLE_COLUMNS.</li>
 *   <li>Standard quoting ({@code "} wrapping, {@code ""} escape); quoting may span
 *       physical lines. Unterminated quote, content after a closing quote, or any
 *       banned character surviving decoding (comma, double-quote, CR, LF) ⇒
 *       MALFORMED_QUOTING.</li>
 *   <li>Trimmed field &gt; {@link #MAX_FIELD_LENGTH} chars ⇒ FIELD_TOO_LONG
 *       (parser resource bound — NOT a change to Domain.register semantics).</li>
 *   <li>Processed data records ≤ {@link #MAX_RECORDS}; exceeding ⇒ file rejection.</li>
 * </ul>
 *
 * <p>Header detection ("domain", case-insensitive first record) is applied by the import
 * service on the first reported row.</p>
 */
public final class DomainCsvParser {

    public static final int MAX_INPUT_BYTES = 256 * 1024;
    public static final int MAX_RECORDS = 2000;
    public static final int MAX_FIELD_LENGTH = 253;

    /** Structural outcome of one processed CSV record. */
    public enum RowKind {
        DOMAIN, MALFORMED_QUOTING, MULTIPLE_COLUMNS, FIELD_TOO_LONG
    }

    /** One processed record: structural kind, 1-based ordinal, decoded value (or ""). */
    public record Row(RowKind kind, int rowNumber, String value) {}

    /** File-level rejection codes for the three frozen non-row outcomes. */
    public enum FileRejection {
        INPUT_TOO_LARGE, INVALID_ENCODING, TOO_MANY_RECORDS
    }

    private DomainCsvParser() {
        // pure static grammar layer
    }

    /**
     * Parses bounded UTF-8 CSV bytes into ordered rows.
     *
     * @throws DomainCsvFileException INPUT_TOO_LARGE / INVALID_ENCODING / TOO_MANY_RECORDS
     */
    public static List<Row> parse(byte[] csvBytes) {
        String text = decode(csvBytes);

        List<Row> rows = new ArrayList<>();
        int processed = 0;
        int i = 0;
        final int n = text.length();
        int rowNumber = 0;

        while (i < n) {
            int recordStart = i;
            boolean inQuotes = false;
            boolean closedQuoteSeen = false;
            boolean strayAfterClose = false;
            int fieldCount = 1;
            StringBuilder value = new StringBuilder();

            while (i < n) {
                char c = text.charAt(i);
                if (inQuotes) {
                    if (c == '"') {
                        if (i + 1 < n && text.charAt(i + 1) == '"') {
                            value.append('"');
                            i += 2;
                        } else {
                            inQuotes = false;
                            closedQuoteSeen = true;
                            i++;
                        }
                    } else {
                        value.append(c);
                        i++;
                    }
                    continue;
                }
                if (c == '"') {
                    inQuotes = true;
                    i++;
                    continue;
                }
                if (c == ',') {
                    fieldCount++;
                    strayAfterClose = false; // a comma restarts field accumulation
                    closedQuoteSeen = false;
                    i++;
                    continue;
                }
                if (c == '\r') {
                    i += (i + 1 < n && text.charAt(i + 1) == '\n') ? 2 : 1;
                    break;
                }
                if (c == '\n') {
                    i++;
                    break;
                }
                if (closedQuoteSeen) {
                    strayAfterClose = true;
                }
                value.append(c);
                i++;
            }

            String raw = text.substring(recordStart, Math.min(i, n));
            if (raw.isBlank()) {
                continue;
            }

            rowNumber++;
            processed++;
            if (processed > MAX_RECORDS) {
                throw new DomainCsvFileException(FileRejection.TOO_MANY_RECORDS);
            }

            RowKind kind;
            String decodedValue;
            if (fieldCount > 1) {
                kind = RowKind.MULTIPLE_COLUMNS;
                decodedValue = "";
            } else if (inQuotes || strayAfterClose) {
                kind = RowKind.MALFORMED_QUOTING;
                decodedValue = "";
            } else {
                String trimmed = value.toString().trim();
                if (trimmed.length() > MAX_FIELD_LENGTH) {
                    kind = RowKind.FIELD_TOO_LONG;
                    decodedValue = trimmed;
                } else if (containsBannedCharacter(trimmed)) {
                    kind = RowKind.MALFORMED_QUOTING;
                    decodedValue = "";
                } else {
                    kind = RowKind.DOMAIN;
                    decodedValue = trimmed;
                }
            }
            rows.add(new Row(kind, rowNumber, decodedValue));
        }
        return rows;
    }

    private static boolean containsBannedCharacter(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == ',' || c == '"' || c == '\r' || c == '\n') {
                return true;
            }
        }
        return false;
    }

    private static String decode(byte[] csvBytes) {
        if (csvBytes == null || csvBytes.length == 0) {
            return "";
        }
        if (csvBytes.length > MAX_INPUT_BYTES) {
            throw new DomainCsvFileException(FileRejection.INPUT_TOO_LARGE);
        }
        java.nio.charset.CharsetDecoder decoder =
            StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT);
        try {
            String text = decoder.decode(java.nio.ByteBuffer.wrap(csvBytes)).toString();
            if (text.startsWith("\uFEFF")) {
                text = text.substring(1);
            }
            return text;
        } catch (CharacterCodingException e) {
            throw new DomainCsvFileException(FileRejection.INVALID_ENCODING);
        }
    }
}
