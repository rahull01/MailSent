package com.mailsent.portfolio.observation.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

import com.mailsent.portfolio.observation.application.DmarcIngestLimits;

/**
 * Aligns the Tomcat connector with the INT-003 5 MiB application cap and a
 * bounded swallow-after-error so oversized bodies do not linger on the socket.
 */
@Component
public class DmarcIngestTomcatCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    /** Bytes Tomcat may drain after an error (413/429) before closing. */
    public static final int MAX_SWALLOW_BYTES = 2 * 1024 * 1024;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addConnectorCustomizers(connector -> {
            connector.setMaxPostSize(DmarcIngestLimits.MAX_HTTP_PAYLOAD_BYTES);
            connector.setProperty("maxSwallowSize", Integer.toString(MAX_SWALLOW_BYTES));
        });
    }
}
