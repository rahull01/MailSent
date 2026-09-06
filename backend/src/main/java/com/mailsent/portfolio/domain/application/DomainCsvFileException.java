package com.mailsent.portfolio.domain.application;

/**
 * DomainCsvFileException — file-level rejection of a DOM-002 CSV import
 * (frozen contract §2/§3): the whole file is refused before any domain is
 * registered.
 *
 * <p>Codes are exactly the frozen {@link DomainCsvParser.FileRejection} values:
 * INPUT_TOO_LARGE · INVALID_ENCODING · TOO_MANY_RECORDS.</p>
 */
public class DomainCsvFileException extends RuntimeException {

    private final DomainCsvParser.FileRejection code;

    public DomainCsvFileException(DomainCsvParser.FileRejection code) {
        super(code.name());
        this.code = code;
    }

    /** The frozen machine-readable rejection code. */
    public DomainCsvParser.FileRejection code() {
        return code;
    }
}
