package com.mailsent.portfolio.observation.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import com.mailsent.portfolio.observation.application.DmarcIngestLimits;

class DmarcIngestSafetyConfigurationTest {

    @Test
    void tomcatCustomizerUsesApplicationFiveMibCap() {
        RecordingFactory factory = new RecordingFactory();
        new DmarcIngestTomcatCustomizer().customize(factory);
        assertEquals(DmarcIngestLimits.MAX_HTTP_PAYLOAD_BYTES, factory.maxPostSize);
        assertEquals(DmarcIngestTomcatCustomizer.MAX_SWALLOW_BYTES, factory.maxSwallowSize);
    }

    @Test
    void applicationYamlCapsTomcatAndHidesActuatorDetails() {
        Map<String, Object> yaml = load("application.yml");
        Map<String, Object> tomcat = map(map(yaml.get("server")).get("tomcat"));
        assertEquals("5MB", String.valueOf(tomcat.get("max-http-form-post-size")));
        assertEquals("2MB", String.valueOf(tomcat.get("max-swallow-size")));
        Map<String, Object> management = map(yaml.get("management"));
        assertEquals("${MANAGEMENT_PORT:8081}", String.valueOf(map(management.get("server")).get("port")));
        Map<String, Object> exposure = map(map(map(management.get("endpoints")).get("web")).get("exposure"));
        assertEquals("health,info", String.valueOf(exposure.get("include")));
        assertEquals("never", String.valueOf(map(map(management.get("endpoint")).get("health")).get("show-details")));
    }

    @Test
    void productionYamlKeepsPrometheusOnManagementPortWithoutDetails() {
        Map<String, Object> yaml = load("application-prod.yml");
        Map<String, Object> management = map(yaml.get("management"));
        assertEquals("${MANAGEMENT_PORT:8081}", String.valueOf(map(management.get("server")).get("port")));
        Object include = map(map(map(management.get("endpoints")).get("web")).get("exposure")).get("include");
        String exposed = String.valueOf(include);
        assertEquals("health,info,prometheus", exposed);
        assertFalse(exposed.contains("metrics"));
        assertEquals("never", String.valueOf(
            map(map(management.get("endpoint")).get("health")).get("show-details")));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> load(String name) {
        try (InputStream in = DmarcIngestSafetyConfigurationTest.class.getClassLoader().getResourceAsStream(name)) {
            return new Yaml().load(in);
        } catch (Exception ex) {
            throw new IllegalStateException(name, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }

    private static final class RecordingFactory
            extends org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory {
        private int maxPostSize;
        private int maxSwallowSize;

        @Override
        public void addConnectorCustomizers(
                org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer... customizers) {
            org.apache.catalina.connector.Connector connector = new org.apache.catalina.connector.Connector();
            for (org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer customizer : List.of(customizers)) {
                customizer.customize(connector);
            }
            this.maxPostSize = connector.getMaxPostSize();
            Object swallow = connector.getProperty("maxSwallowSize");
            this.maxSwallowSize = swallow == null ? -1 : Integer.parseInt(String.valueOf(swallow));
        }
    }
}
