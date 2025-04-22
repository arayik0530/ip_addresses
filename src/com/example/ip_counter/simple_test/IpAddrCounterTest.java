package com.example.ip_counter.simple_test;

import com.example.ip_counter.IpAddrCounter;
import com.example.ip_counter.reader.IpReader;
import com.example.ip_counter.reader.PlainTextIpReader;
import com.example.ip_counter.reader.ZipIpReader;

import java.io.IOException;
import java.nio.file.Path;

public class IpAddrCounterTest {
    public static void main(String[] args) {
        String plainTextFilePath = "src/com/example/ip_counter/simple_test/sample-ips.txt";
        String zipFilePath = "src/com/example/ip_counter/simple_test/sample-ips.zip";

        testIpCountReader(plainTextFilePath, new PlainTextIpReader());
        testIpCountReader(zipFilePath, new ZipIpReader());
    }

    private static void testIpCountReader(String filePath, IpReader reader) {
        try {
            IpAddrCounter counter = new IpAddrCounter();
            reader.read(Path.of(filePath), counter);
            long uniqueCount = counter.count();
            System.out.println("Unique valid IPs: " + uniqueCount);
        } catch (IOException e) {
            System.err.println("Failed to read the file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
        }
    }
}
