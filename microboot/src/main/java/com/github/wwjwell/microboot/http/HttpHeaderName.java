package com.github.wwjwell.microboot.http;

import io.netty.util.AsciiString;

/**
 * Created by wwj on 2017/6/21.
 */
public class HttpHeaderName {
    /**
     * {@code "accept"}
     */
    public static final AsciiString ACCEPT = new AsciiString("Accept");
    /**
     * {@code "accept-Charset"}
     */
    public static final AsciiString ACCEPT_CHARSET = new AsciiString("Accept-Charset");
    /**
     * {@code "accept-Encoding"}
     */
    public static final AsciiString ACCEPT_ENCODING = new AsciiString("Accept-Encoding");
    /**
     * {@code "accept-Language"}
     */
    public static final AsciiString ACCEPT_LANGUAGE = new AsciiString("Accept-Language");
    /**
     * {@code "accept-Ranges"}
     */
    public static final AsciiString ACCEPT_RANGES = new AsciiString("Accept-Ranges");
    /**
     * {@code "accept-Patch"}
     */
    public static final AsciiString ACCEPT_PATCH = new AsciiString("Accept-Patch");
    /**
     * {@code "access-Control-Allow-Credentials"}
     */
    public static final AsciiString ACCESS_CONTROL_ALLOW_CREDENTIALS =
            new AsciiString("Access-Control-Allow-Credentials");
    /**
     * {@code "access-Control-Allow-Headers"}
     */
    public static final AsciiString ACCESS_CONTROL_ALLOW_HEADERS =
            new AsciiString("Access-Control-Allow-Headers");
    /**
     * {@code "access-Control-Allow-Methods"}
     */
    public static final AsciiString ACCESS_CONTROL_ALLOW_METHODS =
            new AsciiString("Access-Control-Allow-Methods");
    /**
     * {@code "access-Control-Allow-Origin"}
     */
    public static final AsciiString ACCESS_CONTROL_ALLOW_ORIGIN =
            new AsciiString("Access-Control-Allow-Origin");
    /**
     * {@code "access-Control-Expose-Headers"}
     */
    public static final AsciiString ACCESS_CONTROL_EXPOSE_HEADERS =
            new AsciiString("Access-Control-Expose-Headers");
    /**
     * {@code "access-Control-Max-Age"}
     */
    public static final AsciiString ACCESS_CONTROL_MAX_AGE = new AsciiString("Access-Control-Max-Age");
    /**
     * {@code "access-Control-Request-Headers"}
     */
    public static final AsciiString ACCESS_CONTROL_REQUEST_HEADERS =
            new AsciiString("Access-Control-Request-Headers");
    /**
     * {@code "access-Control-Request-Method"}
     */
    public static final AsciiString ACCESS_CONTROL_REQUEST_METHOD =
            new AsciiString("Access-Control-Request-Method");
    /**
     * {@code "age"}
     */
    public static final AsciiString AGE = new AsciiString("Age");
    /**
     * {@code "allow"}
     */
    public static final AsciiString ALLOW = new AsciiString("Allow");
    /**
     * {@code "authorization"}
     */
    public static final AsciiString AUTHORIZATION = new AsciiString("Authorization");
    /**
     * {@code "cache-Control"}
     */
    public static final AsciiString CACHE_CONTROL = new AsciiString("Cache-Control");
    /**
     * {@code "connection"}
     */
    public static final AsciiString CONNECTION = new AsciiString("Connection");
    /**
     * {@code "content-Base"}
     */
    public static final AsciiString CONTENT_BASE = new AsciiString("Content-Base");
    /**
     * {@code "content-Encoding"}
     */
    public static final AsciiString CONTENT_ENCODING = new AsciiString("Content-Encoding");
    /**
     * {@code "content-Language"}
     */
    public static final AsciiString CONTENT_LANGUAGE = new AsciiString("Content-Language");
    /**
     * {@code "content-Length"}
     */
    public static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
    /**
     * {@code "content-Location"}
     */
    public static final AsciiString CONTENT_LOCATION = new AsciiString("Content-Location");
    /**
     * {@code "content-Transfer-Encoding"}
     */
    public static final AsciiString CONTENT_TRANSFER_ENCODING = new AsciiString("Content-Transfer-Encoding");
    /**
     * {@code "content-Disposition"}
     */
    public static final AsciiString CONTENT_DISPOSITION = new AsciiString("Content-Disposition");
    /**
     * {@code "content-Md5"}
     */
    public static final AsciiString CONTENT_MD5 = new AsciiString("Content-Md5");
    /**
     * {@code "content-Range"}
     */
    public static final AsciiString CONTENT_RANGE = new AsciiString("Content-Range");
    /**
     * {@code "content-Type"}
     */
    public static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
    /**
     * {@code "cookie"}
     */
    public static final AsciiString COOKIE = new AsciiString("Cookie");
    /**
     * {@code "date"}
     */
    public static final AsciiString DATE = new AsciiString("Date");
    /**
     * {@code "etag"}
     */
    public static final AsciiString ETAG = new AsciiString("Etag");
    /**
     * {@code "expect"}
     */
    public static final AsciiString EXPECT = new AsciiString("Expect");
    /**
     * {@code "expires"}
     */
    public static final AsciiString EXPIRES = new AsciiString("Expires");
    /**
     * {@code "from"}
     */
    public static final AsciiString FROM = new AsciiString("From");
    /**
     * {@code "host"}
     */
    public static final AsciiString HOST = new AsciiString("Host");
    /**
     * {@code "if-Match"}
     */
    public static final AsciiString IF_MATCH = new AsciiString("If-Match");
    /**
     * {@code "if-Modified-Since"}
     */
    public static final AsciiString IF_MODIFIED_SINCE = new AsciiString("If-Modified-Since");
    /**
     * {@code "if-None-Match"}
     */
    public static final AsciiString IF_NONE_MATCH = new AsciiString("If-None-Match");
    /**
     * {@code "if-Range"}
     */
    public static final AsciiString IF_RANGE = new AsciiString("If-Range");
    /**
     * {@code "if-Unmodified-Since"}
     */
    public static final AsciiString IF_UNMODIFIED_SINCE = new AsciiString("If-Unmodified-Since");
    /**
     * @deprecated use {@link #CONNECTION}
     *
     * {@code "keep-Alive"}
     */
    @Deprecated
    public static final AsciiString KEEP_ALIVE = new AsciiString("Keep-Alive");
    /**
     * {@code "last-Modified"}
     */
    public static final AsciiString LAST_MODIFIED = new AsciiString("Last-Modified");
    /**
     * {@code "location"}
     */
    public static final AsciiString LOCATION = new AsciiString("Location");
    /**
     * {@code "max-Forwards"}
     */
    public static final AsciiString MAX_FORWARDS = new AsciiString("Max-Forwards");
    /**
     * {@code "origin"}
     */
    public static final AsciiString ORIGIN = new AsciiString("Origin");
    /**
     * {@code "pragma"}
     */
    public static final AsciiString PRAGMA = new AsciiString("Pragma");
    /**
     * {@code "proxy-Authenticate"}
     */
    public static final AsciiString PROXY_AUTHENTICATE = new AsciiString("Proxy-Authenticate");
    /**
     * {@code "proxy-Authorization"}
     */
    public static final AsciiString PROXY_AUTHORIZATION = new AsciiString("Proxy-Authorization");
    /**
     * @deprecated use {@link #CONNECTION}
     *
     * {@code "proxy-Connection"}
     */
    @Deprecated
    public static final AsciiString PROXY_CONNECTION = new AsciiString("Proxy-Connection");
    /**
     * {@code "range"}
     */
    public static final AsciiString RANGE = new AsciiString("Range");
    /**
     * {@code "referer"}
     */
    public static final AsciiString REFERER = new AsciiString("Referer");
    /**
     * {@code "retry-After"}
     */
    public static final AsciiString RETRY_AFTER = new AsciiString("Retry-After");
    /**
     * {@code "sec-Websocket-Key1"}
     */
    public static final AsciiString SEC_WEBSOCKET_KEY1 = new AsciiString("Sec-Websocket-Key1");
    /**
     * {@code "sec-Websocket-Key2"}
     */
    public static final AsciiString SEC_WEBSOCKET_KEY2 = new AsciiString("Sec-Websocket-Key2");
    /**
     * {@code "sec-Websocket-Location"}
     */
    public static final AsciiString SEC_WEBSOCKET_LOCATION = new AsciiString("Sec-Websocket-Location");
    /**
     * {@code "sec-Websocket-Origin"}
     */
    public static final AsciiString SEC_WEBSOCKET_ORIGIN = new AsciiString("Sec-Websocket-Origin");
    /**
     * {@code "sec-Websocket-Protocol"}
     */
    public static final AsciiString SEC_WEBSOCKET_PROTOCOL = new AsciiString("Sec-Websocket-Protocol");
    /**
     * {@code "sec-Websocket-Version"}
     */
    public static final AsciiString SEC_WEBSOCKET_VERSION = new AsciiString("Sec-Websocket-Version");
    /**
     * {@code "sec-Websocket-Key"}
     */
    public static final AsciiString SEC_WEBSOCKET_KEY = new AsciiString("Sec-Websocket-Key");
    /**
     * {@code "sec-Websocket-Accept"}
     */
    public static final AsciiString SEC_WEBSOCKET_ACCEPT = new AsciiString("Sec-Websocket-Accept");
    /**
     * {@code "sec-Websocket-Protocol"}
     */
    public static final AsciiString SEC_WEBSOCKET_EXTENSIONS = new AsciiString("Sec-Websocket-Extensions");
    /**
     * {@code "server"}
     */
    public static final AsciiString SERVER = new AsciiString("Server");
    /**
     * {@code "set-Cookie"}
     */
    public static final AsciiString SET_COOKIE = new AsciiString("Set-Cookie");
    /**
     * {@code "set-Cookie2"}
     */
    public static final AsciiString SET_COOKIE2 = new AsciiString("Set-Cookie2");
    /**
     * {@code "te"}
     */
    public static final AsciiString TE = new AsciiString("Te");
    /**
     * {@code "trailer"}
     */
    public static final AsciiString TRAILER = new AsciiString("Trailer");
    /**
     * {@code "transfer-Encoding"}
     */
    public static final AsciiString TRANSFER_ENCODING = new AsciiString("Transfer-Encoding");
    /**
     * {@code "upgrade"}
     */
    public static final AsciiString UPGRADE = new AsciiString("Upgrade");
    /**
     * {@code "user-Agent"}
     */
    public static final AsciiString USER_AGENT = new AsciiString("User-Agent");
    /**
     * {@code "vary"}
     */
    public static final AsciiString VARY = new AsciiString("Vary");
    /**
     * {@code "via"}
     */
    public static final AsciiString VIA = new AsciiString("Via");
    /**
     * {@code "warning"}
     */
    public static final AsciiString WARNING = new AsciiString("Warning");
    /**
     * {@code "websocket-Location"}
     */
    public static final AsciiString WEBSOCKET_LOCATION = new AsciiString("Websocket-Location");
    /**
     * {@code "websocket-Origin"}
     */
    public static final AsciiString WEBSOCKET_ORIGIN = new AsciiString("Websocket-Origin");
    /**
     * {@code "websocket-Protocol"}
     */
    public static final AsciiString WEBSOCKET_PROTOCOL = new AsciiString("Websocket-Protocol");
    /**
     * {@code "www-Authenticate"}
     */
    public static final AsciiString WWW_AUTHENTICATE = new AsciiString("Www-Authenticate");

}
