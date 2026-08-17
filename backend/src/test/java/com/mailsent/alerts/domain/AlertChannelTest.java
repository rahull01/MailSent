package com.mailsent.alerts.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AlertChannelTest {

    @Test
    void canonicalChannelsMatchProtocols() {
        assertEquals("in-app", AlertChannel.IN_APP.protocol());
        assertEquals("email", AlertChannel.EMAIL.protocol());
        assertEquals("webhook", AlertChannel.WEBHOOK.protocol());
    }
}
