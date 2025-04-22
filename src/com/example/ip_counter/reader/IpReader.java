package com.example.ip_counter.reader;

import com.example.ip_counter.IpAddrCounter;

import java.io.IOException;
import java.nio.file.Path;

public interface IpReader {
    void read(Path path, IpAddrCounter counter) throws IOException;
}
