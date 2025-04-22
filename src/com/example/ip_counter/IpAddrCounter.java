package com.example.ip_counter;

import java.util.BitSet;

/**
 * Efficient IPv4 address counter using dual BitSet.
 */
public final class IpAddrCounter {

    private final BitSet lower = new BitSet(); // 0 to Integer.MAX_VALUE - 1
    private BitSet upper = null;               // Integer.MAX_VALUE to 2^32 - 1
    private long uniqueCount = 0;

    public void add(String ip) {
        long ipValue = parseIp(ip);

        if (ipValue < Integer.MAX_VALUE) {
            int index = (int) ipValue;
            if (!lower.get(index)) {
                lower.set(index);
                uniqueCount++;
            }
        } else {
            if (upper == null) {
                upper = new BitSet();
            }
            int index = (int) (ipValue - Integer.MAX_VALUE);
            if (!upper.get(index)) {
                upper.set(index);
                uniqueCount++;
            }
        }
    }

    public long count() {
        return uniqueCount;
    }

    private long parseIp(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid IP format: " + ip);
        }

        long result = 0;
        for (int i = 0; i < 4; i++) {
            String part = parts[i];
            int octet;

            try {
                octet = Integer.parseInt(part);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Non-numeric IP segment [" + part + "]");
            }

            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException("Invalid IP segment [" + part + "]");
            }

            result = result * 256 + octet;
        }

        return result;
    }
}
