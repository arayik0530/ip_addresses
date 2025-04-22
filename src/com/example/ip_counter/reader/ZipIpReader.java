package com.example.ip_counter.reader;

import com.example.ip_counter.IpAddrCounter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Reads a ZIP file and feeds valid IPv4 addresses to the counter.
 */
public final class ZipIpReader implements IpReader {

    @Override
    public void read(Path zipPath, IpAddrCounter counter) throws IOException {
        if (zipPath == null || !zipPath.toFile().exists() || !zipPath.toString().endsWith(".zip")) {
            throw new IllegalArgumentException("ZIP file not found or invalid: " + zipPath);
        }

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipPath.toFile())), StandardCharsets.UTF_8)) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    readFromEntry(zis, counter);
                }
                zis.closeEntry();
            }
        }
    }

    private void readFromEntry(InputStream input, IpAddrCounter counter) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
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
