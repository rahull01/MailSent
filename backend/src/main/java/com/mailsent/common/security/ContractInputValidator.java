package com.mailsent.common.security;

import java.util.regex.Pattern;

/**
 * ContractInputValidator — Standard input validation across contract boundaries (SEC-003 / NFR-S4).
 */
public final class ContractInputValidator {

    private static final Pattern DOMAIN_PATTERN = Pattern.compile("^(?!-)[A-Za-z0-9-]{1,63}(?<!-)(\\.[A-Za-z0-9-]{1,63})+$");
    private static final Pattern IPV4_PATTERN = Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$");

    private ContractInputValidator() {}

    public static boolean isValidDomain(String domain) {
        if (domain == null || domain.length() > 253) return false;
        return DOMAIN_PATTERN.matcher(domain.trim()).matches();
    }

    public static boolean isValidIpv4(String ip) {
        if (ip == null) return false;
        return IPV4_PATTERN.matcher(ip.trim()).matches();
    }

    public static String sanitizeString(String input, int maxLength) {
        if (input == null) return "";
        String trimmed = input.trim();
        if (trimmed.length() > maxLength) {
            return trimmed.substring(0, maxLength);
        }
        return trimmed;
    }
}
