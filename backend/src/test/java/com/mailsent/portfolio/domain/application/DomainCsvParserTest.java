package com.mailsent.portfolio.domain.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * DOM-002 frozen CSV grammar tests: structural outcomes, header neutrality, encoding,
 * BOM, banned post-decode characters and resource bounds (frozen contract §2/§3/§9).
 */
class DomainCsvParserTest {

    private static byte[] utf8(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private static List<DomainCsvParser.Row> parse(String csv) {
        return DomainCsvParser.parse(utf8(csv));
    }

    private static DomainCsvParser.FileRejection rejection(String csv) {
        return assertThrows(DomainCsvFileException.class, () -> DomainCsvParser.parse(utf8(csv))).code();
    }

    // ---------- happy paths ----------

    @Test
    void singleDomainParses() {
        var rows = parse("a.com");
        assertEquals(1, rows.size());
        assertEquals(DomainCsvParser.RowKind.DOMAIN, rows.get(0).kind());
        assertEquals("a.com", rows.get(0).value());
        assertEquals(1, rows.get(0).rowNumber());
    }

    @Test
    void multipleDomainsParseInOrder() {
        var rows = parse("a.com\nb.com\nc.com");
        assertEquals(3, rows.size());
        assertEquals(List.of("a.com", "b.com", "c.com"),
            rows.stream().map(DomainCsvParser.Row::value).toList());
    }

    @Test
    void crlfLfAndCrRecordSeparatorsAllWork() {
        assertEquals(4, parse("a.com\r\nb.com\nc.com\rd.com").size());
    }

    @Test
    void quotedDomainIsAccepted() {
        var rows = parse("\"quoted.com\"");
        assertEquals(1, rows.size());
        assertEquals(DomainCsvParser.RowKind.DOMAIN, rows.get(0).kind());
        assertEquals("quoted.com", rows.get(0).value());
    }

    @Test
    void surroundingWhitespaceIsTrimmed() {
        assertEquals("spaced.com", parse("  spaced.com  ").get(0).value());
    }

    @Test
    void bomIsStripped() {
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] body = utf8("bom.com");
        byte[] combined = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(body, 0, combined, bom.length, body.length);
        var rows = DomainCsvParser.parse(combined);
        assertEquals(1, rows.size());
        assertEquals("bom.com", rows.get(0).value());
    }

    @Test
    void emptyInputYieldsNoRows() {
        assertTrue(parse("").isEmpty());
        assertTrue(DomainCsvParser.parse(null).isEmpty());
    }

    @Test
    void blankLinesAreSkippedAndDoNotConsumeRowNumbers() {
        var rows = parse("\n\na.com\n   \n\nb.com\n");
        assertEquals(2, rows.size());
        assertEquals(1, rows.get(0).rowNumber());
        assertEquals(2, rows.get(1).rowNumber());
    }

    // ---------- malformed quoting ----------

    @Test
    void unterminatedQuoteIsMalformed() {
        assertEquals(DomainCsvParser.RowKind.MALFORMED_QUOTING,
            parse("\"unterminated.com").get(0).kind());
    }

    @Test
    void strayQuoteAfterClosingQuoteIsMalformed() {
        assertEquals(DomainCsvParser.RowKind.MALFORMED_QUOTING,
            parse("\"a.com\"x").get(0).kind());
    }

    @Test
    void quoteInsideUnquotedFieldIsMalformed() {
        assertEquals(DomainCsvParser.RowKind.MALFORMED_QUOTING,
            parse("a\"b.com").get(0).kind());
    }

    @Test
    void decodedCommaFromQuotedFieldIsMalformed() {
        // micro-correction: a quoted comma decodes into the field and is then banned
        assertEquals(DomainCsvParser.RowKind.MALFORMED_QUOTING,
            parse("\"example.com,evil.com\"").get(0).kind());
    }

    @Test
    void escapedQuoteSurvivingDecodingIsMalformed() {
        assertEquals(DomainCsvParser.RowKind.MALFORMED_QUOTING,
            parse("\"a\"\"b.com\"").get(0).kind());
    }

    @Test
    void embeddedLineSeparatorInsideQuotedFieldIsMalformed() {
        assertEquals(DomainCsvParser.RowKind.MALFORMED_QUOTING,
            parse("\"a.com\nsecond.line\"").get(0).kind());
        assertEquals(DomainCsvParser.RowKind.MALFORMED_QUOTING,
            parse("\"a.com\r\nsecond.line\"").get(0).kind());
    }

    @Test
    void unquotedCommaCreatesMultipleColumnsFailure() {
        var rows = parse("a.com,b.com");
        assertEquals(1, rows.size(), "the whole record is ONE row failure");
        assertEquals(DomainCsvParser.RowKind.MULTIPLE_COLUMNS, rows.get(0).kind());
    }

    // ---------- field bound ----------

    @Test
    void fieldOfExactly253CharsIsAccepted() {
        String domain = "a".repeat(249) + ".com";
        assertEquals(253, domain.length());
        assertEquals(domain, parse(domain).get(0).value());
    }

    @Test
    void fieldOver253CharsIsRejectedAsTooLong() {
        var rows = parse("a".repeat(250) + ".com");
        assertEquals(DomainCsvParser.RowKind.FIELD_TOO_LONG, rows.get(0).kind());
    }

    // ---------- encoding ----------

    @Test
    void invalidUtf8IsAFileLevelRejection() {
        byte[] bad = {(byte) 0xFF, (byte) 0xFE, 'a', '.', 'c', 'o', 'm'};
        assertEquals(DomainCsvParser.FileRejection.INVALID_ENCODING,
            assertThrows(DomainCsvFileException.class, () -> DomainCsvParser.parse(bad)).code());
    }

    // ---------- limits ----------

    @Test
    void inputExactlyAt256KBCapIsAcceptedStructurally() {
        byte[] bytes = new byte[DomainCsvParser.MAX_INPUT_BYTES];
        // fill with 'a' and a few newlines so we don't exceed MAX_RECORDS
        java.util.Arrays.fill(bytes, (byte) 'a');
        for (int i = 0; i < 100; i++) {
            bytes[i * 2000] = '\n';
        }
        assertTrue(DomainCsvParser.parse(bytes).size() >= 1,
            "an input of exactly the frozen cap is not a file-level rejection");
    }

    @Test
    void inputAbove256KBIsRejectedAsTooLarge() {
        byte[] tooBig = new byte[DomainCsvParser.MAX_INPUT_BYTES + 1];
        assertEquals(DomainCsvParser.FileRejection.INPUT_TOO_LARGE,
            assertThrows(DomainCsvFileException.class, () -> DomainCsvParser.parse(tooBig)).code());
    }

    @Test
    void exactly2000RecordsAreAcceptedStructurally() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < DomainCsvParser.MAX_RECORDS; i++) {
            sb.append("d").append(i).append(".example\n");
        }
        List<DomainCsvParser.Row> rows = parse(sb.toString());
        assertEquals(DomainCsvParser.MAX_RECORDS, rows.size());
        assertEquals(2000, rows.get(rows.size() - 1).rowNumber());
    }

    @Test
    void record2001IsAFileLevelTooManyRecordsRejection() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= DomainCsvParser.MAX_RECORDS; i++) {
            sb.append("d").append(i).append(".example\n");
        }
        assertEquals(DomainCsvParser.FileRejection.TOO_MANY_RECORDS, rejection(sb.toString()));
    }

    // ---------- determinism ----------

    @Test
    void parsingIsDeterministic() {
        var a = parse("x.com\ny.com");
        var b = parse("x.com\ny.com");
        assertEquals(a.size(), b.size());
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i).kind(), b.get(i).kind());
            assertEquals(a.get(i).value(), b.get(i).value());
        }
    }
}
