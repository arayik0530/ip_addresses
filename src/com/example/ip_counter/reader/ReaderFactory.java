package com.example.ip_counter.reader;

import java.nio.file.Path;

public final class ReaderFactory {

    private ReaderFactory() {
    }

    public static IpReader getReader(Path filePath) {
        if (filePath == null || !filePath.toFile().exists()) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        String name = filePath.toString().toLowerCase();
        if (name.endsWith(".zip")) {
            return new ZipIpReader();
        } else if (name.endsWith(".txt")) {
            return new TxtIpReader();
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + filePath);
        }
    }
}
