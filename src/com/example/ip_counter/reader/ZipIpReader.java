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
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * High-performance ZIP reader for a single large text file using NIO & parallel line processing.
 */
public final class ZipIpReader implements IpReader {

    private static final int BUFFER_SIZE = 16 * 1024 * 1024; // 16MB per read
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    @Override
    public void read(Path zipPath, IpAddrCounter counter) throws IOException {
        if (zipPath == null || !zipPath.toFile().exists() || !zipPath.toString().endsWith(".zip")) {
            throw new IllegalArgumentException("ZIP file not found or invalid: " + zipPath);
        }

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<?>> futures = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(zipPath.toFile(), StandardCharsets.UTF_8)) {
            ZipEntry entry = zipFile.entries().nextElement(); // assumes only one file
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

                // Process any final leftover line
                if (leftover.length() > 0) {
                    String last = leftover.toString().trim();
                    if (!last.isBlank()) {
                        futures.add(executor.submit(() -> counter.add(last)));
                    }
                }

                executor.shutdown();
                try {
                    executor.awaitTermination(1, TimeUnit.HOURS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for (Future<?> f : futures) {
                    try {
                        f.get(); // rethrow exceptions if any
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            } catch (ExecutionException e) {
                throw new IOException("Error while processing lines in parallel", e.getCause());
            }
        }
    }

    private void processChunkAsLines(StringBuilder chunkBuilder, IpAddrCounter counter,
                                     ExecutorService executor, List<Future<?>> futures) {
        int start = 0;
        int length = chunkBuilder.length();

        List<String> batch = new ArrayList<>(10_000);

        for (int i = 0; i < length; i++) {
            if (chunkBuilder.charAt(i) == '\n') {
                String line = chunkBuilder.substring(start, i).trim();
                if (!line.isEmpty()) {
                    batch.add(line);
                }

                if (batch.size() >= 10_000) {
                    List<String> finalBatch = new ArrayList<>(batch);
                    futures.add(executor.submit(() -> processLines(finalBatch, counter)));
                    batch.clear();
                }

                start = i + 1;
            }
        }

        // Keep remaining characters for next read
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
                // optionally log invalid IPs
            }
        }
    }
}
