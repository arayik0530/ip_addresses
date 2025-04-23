package com.example.ip_counter.reader;

import com.example.ip_counter.counter.IpCounter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads a plain .txt file using Files.lines()
 * and processes lines in parallel.
 */
public final class TxtIpReader implements IpReader {
    @Override
    public void read(Path txtPath, IpCounter counter) throws IOException {
        if (txtPath == null
                || !Files.exists(txtPath)
                || !txtPath.toString().endsWith(".txt")) {
            throw new IllegalArgumentException(STR."TXT file not found or invalid: \{txtPath}");
        }

        try (var lines = Files.lines(txtPath, StandardCharsets.UTF_8)) {
            lines.parallel().forEach(line -> {
                try {
                    counter.add(line);
                } catch (IllegalArgumentException ignored) {
                    // skip bad lines
                }
            });
        }
    }
}
