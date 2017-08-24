package com.github.wwjwell.microboot.http;

public class HttpVersion {
    private String text;
    private boolean keepAliveDefault;
    private static final String HTTP_1_0_STRING = "HTTP/1.0";
    private static final String HTTP_1_1_STRING = "HTTP/1.1";
    private static final String HTTP_2_STRING = "HTTP/2";

    private HttpVersion(String text,boolean keepAliveDefault) {
        this.text = text;
        this.keepAliveDefault = keepAliveDefault;
    }

    public static final HttpVersion HTTP_1_0 = new HttpVersion(HTTP_1_0_STRING,false);
    public static final HttpVersion HTTP_1_1 = new HttpVersion(HTTP_1_1_STRING, true);
    public static final HttpVersion HTTP_2 = new HttpVersion(HTTP_2_STRING, true);


    public static HttpVersion valueOf(String text) {
        if (text == null) {
            throw new NullPointerException("text");
        }
        text = text.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("text is empty (possibly HTTP/0.9)");
        }
        HttpVersion version = version0(text);
        if (version == null) {
            version = new HttpVersion(text, true);
        }
        return version;
    }
    private static HttpVersion version0(String text) {
        if (HTTP_1_1_STRING.equals(text)) {
            return HTTP_1_1;
        }else if (HTTP_1_0_STRING.equals(text)) {
            return HTTP_1_0;
        }else if(HTTP_2_STRING.equals(text)){
            return HTTP_2;
        }
        return null;
    }

    public boolean isKeepAliveDefault() {
        return keepAliveDefault;
    }

    public String text(){
        return text;
    }

    @Override
    public String toString() {
        return text();
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HttpVersion)) {
            return false;
        }

        HttpVersion that = (HttpVersion) o;
        return text.equals(that);
    }
}
