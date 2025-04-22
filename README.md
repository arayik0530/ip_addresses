# IP Address Counter

> A high-performance Java 21 console application to count unique IPv4 addresses from ZIP or plain‑text files.

## Table of Contents
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Architecture & Performance](#architecture--performance)
    - [Bitset-based Counting](#bitset-based-counting)
    - [Zero-Allocation Parsing](#zero-allocation-parsing)
    - [Parallel I/O and Processing](#parallel-io-and-processing)
- [License](#license)

---

## Features

- Counts unique IPv4 addresses efficiently
- Supports both ZIP and plain‑text input files
- Lock-free, CAS-based updates for thread safety
- Parallel processing leveraging Java’s common ForkJoinPool

---

## Prerequisites

- Java 21
---

## Installation

1. Compile the sources:
   ```bash
   javac -d out \
     src/com/example/ip_counter/*.java \
     src/com/example/ip_counter/reader/*.java
   ```

---

## Usage

1. **Configure the input file**
   In `src/com/example/ip_counter/Application.java`, update:
   ```java
   // ZIP input example
   private static final String DEFAULT_FILE_PATH = "/path/to/ip_addresses.zip";
   // Plain-text input example
   private static final String DEFAULT_FILE_PATH = "/path/to/ip_addresses.txt";
   ```
2. **Run the application**
   ```bash
   java -cp out com.example.ip_counter.Application
   ```

---

## Architecture & Performance

Key design decisions for optimal memory footprint and throughput:

### Bitset-based Counting

- Uses an `AtomicLongArray` of size 2²⁶ (2³² bits) (~512 MB heap)
- Lock-free CAS updates per IP lookup: **O(1)** time, no global locks
- Trade-off: fixed memory allocation for minimal per-IP overhead

### Zero-Allocation Parsing

- `IpParser` splits and converts octets directly into a `long`
- Single-pass validation minimizes branching and exception overhead

### Parallel I/O and Processing

- `TxtIpReader` employs `Files.lines(...).parallel()` for file streams
- `ZipIpReader` streams ZIP entries, then processes lines in parallel
- JDK’s implicit batching reduces coordination overhead across threads

---

