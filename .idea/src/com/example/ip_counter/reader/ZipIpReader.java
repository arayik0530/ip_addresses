package com.example.ip_counter.reader;

import com.example.ip_counter.IpCounter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads the first non‑directory entry in a ZIP and processes
 * its lines in parallel via Stream.parallel().
 */
public final class ZipIpReader implements IpReader {
    @Override
    public void read(Path zipPath, IpCounter counter) throws IOException {
        try (var zipFile = new ZipFile(zipPath.toFile())) {
            var entries = zipFile.stream()
                    .filter(e -> !e.isDirectory())
                    .toList();
            if (entries.isEmpty()) {
                throw new IOException(STR."ZIP contains no files: \{zipPath}");
            }

            ZipEntry entry = entries.getFirst();
            try (var is = zipFile.getInputStream(entry);
                 var reader = new BufferedReader(
                         new InputStreamReader(is, StandardCharsets.UTF_8))
            ) {
                reader.lines()
                        .parallel()
                        .forEach(line -> {
                            try {
                                counter.add(line);
                            } catch (IllegalArgumentException ignored) {
                                // skip bad lines
                            }
                        });
            }
        }
    }
}
