package com.example.ip_counter;

/**
 * Utility to parse dotted‑decimal IPv4 strings to unsigned 32‑bit values.
 */
public final class IpParser {
    private IpParser() {
    }

    /**
     * @param ip dotted‑decimal IPv4 (e.g. "203.0.113.5")
     * @return 0 ≤ long ≤ 2^32‑1
     * @throws IllegalArgumentException if invalid format or octet range
     */
    public static long parseIp(String ip) {
        var parts = ip.split("\\.", 4);
        if (parts.length != 4) {
            throw new IllegalArgumentException(STR."Invalid IP format: \{ip}");
        }

        long result = 0;
        for (var part : parts) {
            int octet = Integer.parseInt(part);
            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException(STR."Invalid IP segment: \{part}");
            }
            result = (result << 8) | octet;
        }
        return result;
    }
}
