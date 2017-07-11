package com.github.wwjwell.microboot.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通过requestId能够知道大致请求的时间
 *
 * <pre>
 * 		目前是 currentTimeMillis * (2^20) + offset.incrementAndGet()
 * 		通过 requestId / (2^20 * 1000) 能够得到秒
 * </pre>
 * Created by wwj on 2017/6/29.
 */
public class RequestIdGenerator {
    protected static final AtomicInteger offset = new AtomicInteger(0);
    protected static final int BITS = 20;
    protected static final int MAX_COUNT_PER_MILLIS = 1 << BITS;

    /**
     * 获取 requestId
     *
     * @return
     */
    public static long getRequestId() {
        long currentTime = System.currentTimeMillis();
        int count = offset.incrementAndGet();
        while(count >= MAX_COUNT_PER_MILLIS){
            if(offset.get() >= MAX_COUNT_PER_MILLIS) {
                synchronized (RequestIdGenerator.class) {
                    if (offset.get() >= MAX_COUNT_PER_MILLIS) {
                        offset.set(0);
                    }
                }
            }
            count = offset.incrementAndGet();
        }
        return (currentTime << BITS) + count;
    }

    public static long getTimeByRequestId(long reqId) {
        return reqId >>> BITS;
    }
}
