package com.example.ip_counter;

import com.example.ip_counter.reader.IpReader;
import com.example.ip_counter.reader.ReaderFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Application entry point.
 * Author: Ara Gevorgyan
 * Created on: 4/22/25
 */
public final class Application {

    private static final String DEFAULT_FILE_PATH = "/home/aragevorgyan/Downloads/ip_addresses.zip";
//    private static final String DEFAULT_FILE_PATH = "/home/aragevorgyan/Downloads/ip_addresses.txt";

    public static void main(String[] args) {
        Path inputPath = Path.of(DEFAULT_FILE_PATH);
        IpReader reader = ReaderFactory.getReader(inputPath);

        IpAddrCounter counter = new IpAddrCounter();

        System.out.println(STR."Reading from: \{inputPath}");

        Instant start = Instant.now();
        try {
            reader.read(inputPath, counter);
        } catch (Exception e) {
            System.err.println(STR."❌ Error while reading IPs: \{e.getMessage()}");
            return;
        }
        Instant end = Instant.now();

        System.out.printf("✅ Unique IPv4 addresses: %,d%n", counter.count());
        System.out.printf("⏱️ Time taken: %.2f seconds%n", Duration.between(start, end).toMillis() / 1000.0);
    }
}
