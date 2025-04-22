package com.example.ip_counter;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Lock-free IPv4 address counter using atomic operations.
 */
public final class IpAddrCounter {

    private static final int BITSET_SIZE = (int) (Integer.MAX_VALUE / 64L + 1); // each long holds 64 bits

    private final AtomicLongArray lower = new AtomicLongArray(BITSET_SIZE);
    private final AtomicLongArray upper = new AtomicLongArray(BITSET_SIZE);
    private final AtomicLong uniqueCount = new AtomicLong(0);

    public void add(String ip) {
        long ipValue = parseIp(ip);
        boolean updated = false;

        if (ipValue <= Integer.MAX_VALUE) {
            updated = trySetBit(lower, ipValue);
        } else {
            long upperIndex = ipValue - (long) Integer.MAX_VALUE - 1;
            updated = trySetBit(upper, upperIndex);
        }

        if (updated) {
            uniqueCount.incrementAndGet();
        }
    }

    public long count() {
        return uniqueCount.get();
    }

    private boolean trySetBit(AtomicLongArray bitset, long bitIndex) {
        int longIndex = (int) (bitIndex / 64);
        long mask = 1L << (bitIndex % 64);

        while (true) {
            long current = bitset.get(longIndex);
            if ((current & mask) != 0) {
                return false; // already set
            }
            long updated = current | mask;
            if (bitset.compareAndSet(longIndex, current, updated)) {
                return true;
            }
        }
    }

    private long parseIp(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid IP format: " + ip);
        }

        long result = 0;
        for (int i = 0; i < 4; i++) {
            int octet = Integer.parseInt(parts[i]);
            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException("Invalid IP segment [" + parts[i] + "]");
            }
            result = (result << 8) + octet;
        }
        return result;
    }
}
