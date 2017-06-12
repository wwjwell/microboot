package com.zhuanglide.micrboot.metrics;

public class BytesMetrics {
    private long readBytes = 0;
    private long wroteBytes = 0;

    void incrementRead(long numBytes) {
        readBytes += numBytes;
    }

    void incrementWrote(long numBytes) {
        wroteBytes += numBytes;
    }

    public long readBytes() {
        return readBytes;
    }

    public long wroteBytes() {
        return wroteBytes;
    }
}