package com.zhuanglide.micrboot.constants;

import io.netty.util.AttributeKey;

/**
 * Created by wwj on 2017/6/9.
 */
public class Constants {
    public static final String REQ_ID = "NETTY_REQ_ID";
    public static final AttributeKey<Long> ATTR_REQ_ID =  AttributeKey.valueOf(Constants.REQ_ID);
    public static final AttributeKey<Long> ATTR_CONN_ACTIVE_TIME =  AttributeKey.valueOf("ATTR_CONN_ACTIVE_TIME");

}
