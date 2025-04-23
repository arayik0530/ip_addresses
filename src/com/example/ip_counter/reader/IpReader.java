package com.example.ip_counter.reader;

import com.example.ip_counter.counter.IpCounter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Reads all lines from the given file and feeds them into an IpCounter.
 */
public interface IpReader {
    void read(Path path, IpCounter counter) throws IOException;
}
