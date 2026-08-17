package com.mailsent.portfolio.observation.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.mailsent.portfolio.observation.application.SmtpProbeResult;
import com.mailsent.portfolio.observation.domain.SmtpProbeOutcome;

/**
 * Local-loopback tests for {@link SocketSmtpProbe}. Uses an ephemeral
 * {@link ServerSocket} — never mail send, AUTH, or port 25 of a third party.
 */
class SocketSmtpProbeTest {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ServerSocket server;

    @AfterEach
    void tearDown() throws Exception {
        if (server != null && !server.isClosed()) {
            server.close();
        }
        executor.shutdownNow();
        executor.awaitTermination(2, TimeUnit.SECONDS);
    }

    @Test
    void successfulProbeReadsGreetingSendsEhloWithDomainThenQuitAndNeverSendsMailCommands() throws Exception {
        server = new ServerSocket(0);
        int port = server.getLocalPort();
        List<String> commands = new CopyOnWriteArrayList<>();
        executor.submit(() -> {
            try {
                serve(server, commands,
                    "220 mail.example.com ESMTP\r\n",
                    "250-PIPELINING\r\n250 HELP\r\n",
                    "221 bye\r\n");
            } catch (Exception ignored) {
                // closed by test teardown
            }
        });

        SocketSmtpProbe probe = new SocketSmtpProbe(port, 2_000, 2_000);
        SmtpProbeResult result = probe.probe("127.0.0.1", "example.com");

        assertEquals(SmtpProbeOutcome.CONNECTED, result.outcome());
        assertEquals("220 mail.example.com ESMTP", result.rawGreeting());
        assertTrue(result.rawEhlo().contains("250-PIPELINING"));
        assertTrue(result.rawEhlo().contains("250 HELP"));
        assertEquals(List.of("EHLO example.com", "QUIT"), commands);
        assertFalse(commands.stream().anyMatch(c -> c.startsWith("MAIL")));
        assertFalse(commands.stream().anyMatch(c -> c.startsWith("AUTH")));
        assertFalse(commands.stream().anyMatch(c -> c.startsWith("RCPT")));
        assertFalse(commands.stream().anyMatch(c -> c.startsWith("DATA")));
    }

    @Test
    void connectionRefusalHasNoFabricatedGreeting() throws Exception {
        int closedPort;
        try (ServerSocket closed = new ServerSocket(0)) {
            closedPort = closed.getLocalPort();
        }
        SocketSmtpProbe probe = new SocketSmtpProbe(closedPort, 500, 500);
        SmtpProbeResult result = probe.probe("127.0.0.1", "example.com");

        assertEquals(SmtpProbeOutcome.REFUSED, result.outcome());
        assertNull(result.rawGreeting());
        assertNull(result.rawEhlo());
    }

    @Test
    void readTimeoutHasNoFabricatedGreeting() throws Exception {
        server = new ServerSocket(0);
        int port = server.getLocalPort();
        executor.submit(() -> {
            try (Socket ignored = server.accept()) {
                Thread.sleep(2_000);
            } catch (Exception ignored) {
                // closed by test teardown
            }
            return null;
        });

        SocketSmtpProbe probe = new SocketSmtpProbe(port, 500, 200);
        SmtpProbeResult result = probe.probe("127.0.0.1", "example.com");

        assertEquals(SmtpProbeOutcome.TIMEOUT, result.outcome());
        assertNull(result.rawGreeting());
        assertNull(result.rawEhlo());
    }

    private static void serve(ServerSocket server, List<String> commands, String greeting, String ehlo, String quit)
            throws Exception {
        try (Socket client = server.accept()) {
            BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(client.getOutputStream(), StandardCharsets.ISO_8859_1));
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(client.getInputStream(), StandardCharsets.ISO_8859_1));
            writer.write(greeting);
            writer.flush();
            String first = reader.readLine();
            if (first != null) {
                commands.add(first);
            }
            writer.write(ehlo);
            writer.flush();
            String second = reader.readLine();
            if (second != null) {
                commands.add(second);
            }
            writer.write(quit);
            writer.flush();
        }
    }
}
