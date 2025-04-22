package com.example.ip_counter.reader;

import com.example.ip_counter.IpAddrCounter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * High-performance reader for plain .txt files using NIO and multithreading.
 */
public final class TxtIpReader implements IpReader {

    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int BATCH_SIZE = 10_000;

    @Override
    public void read(Path txtPath, IpAddrCounter counter) throws IOException {
        if (txtPath == null || !Files.exists(txtPath) || !txtPath.toString().endsWith(".txt")) {
            throw new IllegalArgumentException("TXT file not found or invalid: " + txtPath);
        }

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<?>> futures = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(txtPath, StandardCharsets.UTF_8)) {
            List<String> batch = new ArrayList<>(BATCH_SIZE);
            String line;

            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    batch.add(trimmed);
                }

                if (batch.size() >= BATCH_SIZE) {
                    List<String> finalBatch = new ArrayList<>(batch);
                    futures.add(executor.submit(() -> processLines(finalBatch, counter)));
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                List<String> finalBatch = new ArrayList<>(batch);
                futures.add(executor.submit(() -> processLines(finalBatch, counter)));
            }

            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new IOException("Error while processing lines", e);
                }
            }

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
