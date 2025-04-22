package com.example.ip_counter.reader;

import com.example.ip_counter.IpAddrCounter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * High-performance ZIP reader for a single large text file using NIO & virtual threads.
 */
public final class ZipIpReader implements IpReader {

    private static final int BUFFER_SIZE = 16 * 1024 * 1024; // 16 MB buffer
    private static final int BATCH_SIZE = 10_000;

    @Override
    public void read(Path zipPath, IpAddrCounter counter) throws IOException {
        if (zipPath == null || !zipPath.toFile().exists() || !zipPath.toString().endsWith(".zip")) {
            throw new IllegalArgumentException(STR."ZIP file not found or invalid: \{zipPath}");
        }

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
             ZipFile zipFile = new ZipFile(zipPath.toFile(), StandardCharsets.UTF_8)) {

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            if (!entries.hasMoreElements()) {
                throw new IOException("ZIP file is empty");
            }

            ZipEntry entry = entries.nextElement(); // assumes only one file
            List<Future<?>> futures = new ArrayList<>();

            try (InputStream inputStream = zipFile.getInputStream(entry);
                 ReadableByteChannel channel = Channels.newChannel(inputStream)) {

                ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.IGNORE)
                        .onUnmappableCharacter(CodingErrorAction.IGNORE);

                StringBuilder leftover = new StringBuilder();

                while (channel.read(byteBuffer) > 0) {
                    byteBuffer.flip();
                    CharBuffer charBuffer = decoder.decode(byteBuffer);
                    leftover.append(charBuffer);
                    byteBuffer.clear();

                    processChunkAsLines(leftover, counter, executor, futures);
                }

                // Handle final leftover content
                if (!leftover.isEmpty()) {
                    String last = leftover.toString().trim();
                    if (!last.isBlank()) {
                        futures.add(executor.submit(() -> processLines(List.of(last), counter)));
                    }
                }

                // Wait for all submitted tasks
                for (Future<?> f : futures) {
                    try {
                        f.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new IOException("Error while processing lines in parallel", e);
                    }
                }

            }
        }
    }

    private void processChunkAsLines(StringBuilder chunkBuilder, IpAddrCounter counter,
                                     ExecutorService executor, List<Future<?>> futures) {

        int start = 0;
        int length = chunkBuilder.length();
        List<String> batch = new ArrayList<>(BATCH_SIZE);

        for (int i = 0; i < length; i++) {
            if (chunkBuilder.charAt(i) == '\n') {
                String line = chunkBuilder.substring(start, i).trim();
                if (!line.isEmpty()) {
                    batch.add(line);
                }

                if (batch.size() >= BATCH_SIZE) {
                    List<String> finalBatch = new ArrayList<>(batch);
                    futures.add(executor.submit(() -> processLines(finalBatch, counter)));
                    batch.clear();
                }

                start = i + 1;
            }
        }

        if (start < length) {
            chunkBuilder.delete(0, start);
        } else {
            chunkBuilder.setLength(0);
        }

        if (!batch.isEmpty()) {
            List<String> finalBatch = new ArrayList<>(batch);
            futures.add(executor.submit(() -> processLines(finalBatch, counter)));
        }
    }

    private void processLines(List<String> lines, IpAddrCounter counter) {
        for (String line : lines) {
            try {
                counter.add(line);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}
