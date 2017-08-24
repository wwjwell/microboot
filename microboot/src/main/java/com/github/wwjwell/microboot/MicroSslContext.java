package com.github.wwjwell.microboot;

import com.github.wwjwell.microboot.util.JettyTlsUtil;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
public class MicroSslContext {
    /*
     * List of ALPN/NPN protocols in order of preference. MICRO_EXP_VERSION
     * requires that HTTP2_VERSION be present and that MICRO_EXP_VERSION should be
     * preferenced over HTTP2_VERSION.
     */
    static final List<String> NEXT_PROTOCOL_VERSIONS =
            Collections.unmodifiableList(Arrays.asList(ApplicationProtocolNames.HTTP_1_1, ApplicationProtocolNames.HTTP_2));

    /*
     * These configs use ACCEPT due to limited support in OpenSSL.  Actual protocol enforcement is
     * done in ProtocolNegotiators.
     */
    private static final ApplicationProtocolConfig ALPN = new ApplicationProtocolConfig(
            Protocol.ALPN,
            SelectorFailureBehavior.NO_ADVERTISE,
            SelectedListenerFailureBehavior.ACCEPT,
            NEXT_PROTOCOL_VERSIONS);

    private static final ApplicationProtocolConfig NPN = new ApplicationProtocolConfig(
            Protocol.NPN,
            SelectorFailureBehavior.NO_ADVERTISE,
            SelectedListenerFailureBehavior.ACCEPT,
            NEXT_PROTOCOL_VERSIONS);

    private static final ApplicationProtocolConfig NPN_AND_ALPN = new ApplicationProtocolConfig(
            Protocol.NPN_AND_ALPN,
            SelectorFailureBehavior.NO_ADVERTISE,
            SelectedListenerFailureBehavior.ACCEPT,
            NEXT_PROTOCOL_VERSIONS);

    /**
     * Creates a SslContextBuilder with ciphers and APN appropriate for micro.
     *
     * @see SslContextBuilder#forClient()
     * @see #configure(SslContextBuilder)
     */
    public static SslContextBuilder forClient() {
        return configure(SslContextBuilder.forClient());
    }

    /**
     * Creates a SslContextBuilder with ciphers and APN appropriate for micro.
     *
     * @see SslContextBuilder#forServer(File, File)
     * @see #configure(SslContextBuilder)
     */
    public static SslContextBuilder forServer(File keyCertChainFile, File keyFile) {
        return configure(SslContextBuilder.forServer(keyCertChainFile, keyFile));
    }

    /**
     *
     * @param keyCertChainFilePath
     * @param keyFilePath
     * @param keyPassword
     * @param trustCertCollectionFilePath
     * @return
     */
    public static SslContextBuilder forServer(String keyCertChainFilePath, String keyFilePath, String keyPassword,String trustCertCollectionFilePath) {
        File keyCertChainFile = new File(keyCertChainFilePath);
        File keyFile = new File(keyFilePath);
        SslContextBuilder builder = configure(SslContextBuilder.forServer(keyCertChainFile, keyFile));
        if (null != trustCertCollectionFilePath) {
            File trustCertCollectionFile = new File(trustCertCollectionFilePath);
            builder.trustManager(trustCertCollectionFile);
        }
        return builder;
    }

    /**
     * Creates a with ciphers and APN appropriate for micro.
     *
     * @param keyCertChainFile an X.509 certificate chain file in PEM format
     * @param keyFile a PKCS#8 private key file in PEM format
     * @param keyPassword the password of the {@code keyFile}, or {@code null} if it's not
     * @see #configure(SslContextBuilder)
     */
    public static SslContextBuilder forServer(
            File keyCertChainFile, File keyFile, String keyPassword) {
        SslContextBuilder builder = configure(SslContextBuilder.forServer(keyCertChainFile, keyFile, keyPassword));
        return builder;
    }

    /**
     * Set ciphers and APN appropriate for micro. Precisely what is set is permitted to change, so if
     * an application requires particular settings it should override the options set here.
     */
    public static SslContextBuilder configure(SslContextBuilder builder) {
        return configure(builder, defaultSslProvider());
    }

    /**
     * Set ciphers and APN appropriate for micro. Precisely what is set is permitted to change, so if
     * an application requires particular settings it should override the options set here.
     */
    public static SslContextBuilder configure(SslContextBuilder builder, SslProvider provider) {
        return builder.sslProvider(provider)
                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .applicationProtocolConfig(selectApplicationProtocolConfig(provider));
    }

    /**
     * Returns OpenSSL if available, otherwise returns the JDK provider.
     */
    private static SslProvider defaultSslProvider() {
        return OpenSsl.isAvailable() ? SslProvider.OPENSSL : SslProvider.JDK;
    }

    /**
     * Attempts to select the best {@link ApplicationProtocolConfig} for the given
     * {@link SslProvider}.
     */
    private static ApplicationProtocolConfig selectApplicationProtocolConfig(SslProvider provider) {
        switch (provider) {
            case JDK: {
                if (JettyTlsUtil.isJettyAlpnConfigured()) {
                    return ALPN;
                }
                if (JettyTlsUtil.isJettyNpnConfigured()) {
                    return NPN;
                }
                throw new IllegalArgumentException("Jetty ALPN/NPN has not been properly configured.");
            }
            case OPENSSL: {
                if (!OpenSsl.isAvailable()) {
                    throw new IllegalArgumentException("OpenSSL is not installed on the system.");
                }

                if (OpenSsl.isAlpnSupported()) {
                    return NPN_AND_ALPN;
                } else {
                    return NPN;
                }
            }
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }
}
