package com.github.wwjwell.microboot.util;

public class JettyTlsUtil {
    private JettyTlsUtil() {
    }

    /**
     * Indicates whether or not the Jetty ALPN jar is installed in the boot classloader.
     */
    public static boolean isJettyAlpnConfigured() {
        try {
            Class.forName("org.eclipse.jetty.alpn.ALPN", true, null);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Indicates whether or not the Jetty NPN jar is installed in the boot classloader.
     */
    public static boolean isJettyNpnConfigured() {
        try {
            Class.forName("org.eclipse.jetty.npn.NextProtoNego", true, null);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
