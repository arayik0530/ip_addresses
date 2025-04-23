package com.example.ip_counter.simple_test;

import com.example.ip_counter.counter.IpCounter;
import com.example.ip_counter.counter.Ipv4AddressCounter;
import com.example.ip_counter.reader.IpReader;
import com.example.ip_counter.reader.TxtIpReader;
import com.example.ip_counter.reader.ZipIpReader;

import java.io.IOException;
import java.nio.file.Path;

public class IpAddrCounterTest {
    public static void main(String[] args) {
        String plainTextFilePath = "src/com/example/ip_counter/simple_test/sample-ips.txt";
        String zipFilePath = "src/com/example/ip_counter/simple_test/sample-ips.zip";

        runTest(plainTextFilePath, new TxtIpReader());
        runTest(zipFilePath, new ZipIpReader());
    }

    private static void runTest(String filePath, IpReader reader) {
        try {
            IpCounter counter = new Ipv4AddressCounter();
            reader.read(Path.of(filePath), counter);
            long uniqueCount = counter.count();

            if (uniqueCount != 8) {
                throw new AssertionError(STR."Expected 8 unique IPs but found: \{uniqueCount}");
            }

            System.out.println("Test passed!");

        } catch (IOException e) {
            System.err.println(STR."Failed to read the file: \{e.getMessage()}");
        } catch (Exception e) {
            System.err.println(STR."Error occurred: \{e.getMessage()}");
        }
    }
}
