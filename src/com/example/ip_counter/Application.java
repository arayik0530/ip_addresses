package com.example.ip_counter;

import com.example.ip_counter.reader.IpReader;
import com.example.ip_counter.reader.ZipIpReader;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Application entry point.
 * Author: Ara Gevorgyan
 * Created on: 4/19/25
 */
public final class Application {

    private static final String DEFAULT_FILE_PATH = "/home/aragevorgyan/Downloads/ip_addresses.zip";

    public static void main(String[] args) {
        Path inputPath = Path.of(DEFAULT_FILE_PATH);
        IpReader reader = new ZipIpReader();

        IpAddrCounter counter = new IpAddrCounter();

        System.out.println("Reading from: " + inputPath);

        Instant start = Instant.now();
        try {
            reader.read(inputPath, counter);
        } catch (Exception e) {
            System.err.println("❌ Error while reading IPs: " + e.getMessage());
            return;
        }
        Instant end = Instant.now();

        System.out.printf("✅ Unique IPv4 addresses: %,d%n", counter.count());
        System.out.printf("⏱️ Time taken: %.2f seconds%n", Duration.between(start, end).toMillis() / 1000.0);
    }
}
