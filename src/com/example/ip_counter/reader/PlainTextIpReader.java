package com.example.ip_counter.reader;

import com.example.ip_counter.IpAddrCounter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PlainTextIpReader implements IpReader {

    @Override
    public void read(Path path, IpAddrCounter counter) throws IOException {
        if (!Files.exists(path) || !path.toString().endsWith(".txt")) {
            throw new IllegalArgumentException("TXT file not found or invalid: " + path);
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            long linesRead = 0;
            while ((line = reader.readLine()) != null) {
                linesRead++;
                String trimmed = line.trim();
                if (!trimmed.isBlank()) {
                    try {
                        counter.add(trimmed);
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        }
    }
}
