package com.example.ip_counter;

/**
 * Counts unique IPv4 addresses.
 */
public interface IpCounter {
    /**
     * Adds one IPv4 address (dotted‑decimal). If it was not seen before, it increments the unique count.
     *
     * @param ip dotted‑decimal IPv4 string, e.g. "192.168.0.1"
     * @throws IllegalArgumentException if the format is invalid
     */
    void add(String ip);

    /**
     * @return number of unique addresses seen so far
     */
    long count();
}
