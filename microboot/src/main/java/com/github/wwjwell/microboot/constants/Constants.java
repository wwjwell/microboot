package com.github.wwjwell.microboot.constants;

import io.netty.util.AttributeKey;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wwj on 2017/6/9.
 */
public class Constants {
    public static final String REQ_ID = "NETTY_REQ_ID";
    public static final AttributeKey<Long> ATTR_REQ_ID =  AttributeKey.newInstance(Constants.REQ_ID);
    public static final AttributeKey<Boolean> ATTR_KEEP_ALIVE =  AttributeKey.newInstance("ATTR_KEEP_ALIVE");
    public static final AttributeKey<AtomicInteger> ATTR_HTTP_REQ_TIMES = AttributeKey.newInstance("ATTR_HTTP_REQ_TIMES");
}
