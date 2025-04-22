package com.example.ip_counter;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Lock‑free, bitset‑based IPv4 counter.
 * Uses a single AtomicLongArray of 2^26 longs (2^32 bits) and an AtomicLong for the unique count.
 */
public final class Ipv4AddressCounter implements IpCounter {
    // 2^32 bits / 64 bits-per-long = 2^26 longs
    private static final int BITSET_SIZE = 1 << 26;

    private final AtomicLongArray bits = new AtomicLongArray(BITSET_SIZE);
    private final AtomicLong uniqueCount = new AtomicLong();

    @Override
    public void add(String ip) {
        long value = IpParser.parseIp(ip);
        int idx = (int) (value >>> 6);         // divide by 64
        long mask = 1L << (value & 0x3F);      // mod 64
        while (true) {
            long current = bits.get(idx);
            if ((current & mask) != 0) {
                // already counted
                return;
            }
            long updated = current | mask;
            if (bits.compareAndSet(idx, current, updated)) {
                uniqueCount.incrementAndGet();
                return;
            }
        }
    }

    @Override
    public long count() {
        return uniqueCount.get();
    }
}
