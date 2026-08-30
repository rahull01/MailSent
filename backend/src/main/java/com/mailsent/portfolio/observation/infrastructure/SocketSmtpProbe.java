package com.mailsent.portfolio.observation.infrastructure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import com.mailsent.portfolio.observation.application.SmtpProbe;
import com.mailsent.portfolio.observation.application.SmtpProbeResult;

/**
 * SocketSmtpProbe — production {@link SmtpProbe} using a bounded JDK
 * {@link Socket} to {@code ip}:25. Connect and read timeouts apply. After a
 * greeting is observed, EHLO is sent with the registered domain name as the
 * protocol argument, then QUIT. MAIL FROM, AUTH, RCPT, and DATA are never
 * sent. Greeting and EHLO text are never fabricated.
 */
public class SocketSmtpProbe implements SmtpProbe {

    static final int SMTP_PORT = 25;
    static final int DEFAULT_CONNECT_TIMEOUT_MS = 5_000;
    static final int DEFAULT_READ_TIMEOUT_MS = 5_000;

    private final int port;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public SocketSmtpProbe() {
        this(SMTP_PORT, DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS);
    }

    SocketSmtpProbe(int port, int connectTimeoutMs, int readTimeoutMs) {
        this.port = port;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    @Override
    public SmtpProbeResult probe(String ip, String ehloDomain) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), connectTimeoutMs);
            socket.setSoTimeout(readTimeoutMs);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
            BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.ISO_8859_1));

            String greeting = readReply(reader);
            String ehlo = null;
            if (greeting != null) {
                ehlo = exchangeEhloThenQuit(writer, reader, ehloDomain);
            } else {
                greeting = "";
                sendQuietly(writer, "QUIT");
            }
            return SmtpProbeResult.connected(greeting, ehlo);
        } catch (ConnectException ex) {
            return SmtpProbeResult.refused();
        } catch (SocketTimeoutException ex) {
            return SmtpProbeResult.timeout();
        } catch (SocketException ex) {
            return isRefused(ex) ? SmtpProbeResult.refused() : SmtpProbeResult.unavailable();
        } catch (IOException ex) {
            if (isRefused(ex)) {
                return SmtpProbeResult.refused();
            }
            return SmtpProbeResult.unavailable();
        }
    }

    private String exchangeEhloThenQuit(BufferedWriter writer, BufferedReader reader, String ehloDomain)
            throws IOException {
        String ehlo = null;
        try {
            writer.write("EHLO " + ehloDomain + "\r\n");
            writer.flush();
            ehlo = readReply(reader);
        } catch (IOException ex) {
            sendQuietly(writer, "QUIT");
            return null;
        }
        sendQuietly(writer, "QUIT");
        try {
            readReply(reader);
        } catch (IOException ignored) {
            // QUIT reply is optional; the greeting/EHLO already observed stand.
        }
        return ehlo;
    }

    private static void sendQuietly(BufferedWriter writer, String verb) {
        try {
            writer.write(verb + "\r\n");
            writer.flush();
        } catch (IOException ignored) {
            // Best-effort close of the SMTP session.
        }
    }

    private static boolean isRefused(IOException ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof ConnectException) {
                return true;
            }
            String message = current.getMessage();
            if (message != null && message.toLowerCase().contains("refused")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    /** A hostile MX can stream replies forever; the probe reads at most this much. */
    private static final int MAX_REPLY_CHARS = 64 * 1024;
    private static final int MAX_REPLY_LINES = 256;

    /**
     * Reads one SMTP reply (single line, or multi-line while lines continue with
     * {@code "NNN-"}). Returns {@code null} when the peer sent nothing or exceeded the
     * reply bounds — an over-large or unbounded reply is treated as no usable reply,
     * never truncated into a fabricated greeting.
     */
    private static String readReply(BufferedReader reader) throws IOException {
        StringBuilder reply = new StringBuilder();
        int lines = 0;
        while (lines < MAX_REPLY_LINES) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            if (reply.length() > 0) {
                reply.append('\n');
            }
            reply.append(line);
            lines++;
            if (line.length() < 4 || line.charAt(3) == ' ') {
                break;
            }
            if (reply.length() > MAX_REPLY_CHARS) {
                // Bound exceeded mid-multi-line reply: unusable as an observed fact.
                return null;
            }
        }
        if (!reply.isEmpty() && reply.length() > MAX_REPLY_CHARS) {
            return null;
        }
        return reply.isEmpty() ? null : reply.toString();
    }
}
