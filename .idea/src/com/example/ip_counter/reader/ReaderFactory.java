package com.example.ip_counter.reader;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Factory to pick TXT vs ZIP reader based on file extension.
 */
public final class ReaderFactory {
    private ReaderFactory() {
    }

    public static IpReader getReader(Path path) {
        if (path == null || !Files.exists(path)) {
            throw new IllegalArgumentException(STR."File not found: \{path}");
        }
        var name = path.toString().toLowerCase();
        if (name.endsWith(".zip")) {
            return new ZipIpReader();
        }
        if (name.endsWith(".txt")) {
            return new TxtIpReader();
        }
        throw new IllegalArgumentException(STR."Unsupported file type: \{path}");
    }
}
