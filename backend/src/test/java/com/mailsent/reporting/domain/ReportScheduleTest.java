package com.mailsent.reporting.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.mailsent.alerts.domain.AlertChannel;

class ReportScheduleTest {

    @Test
    void configuresValidSchedule() {
        String targetId = UUID.randomUUID().toString();
        
        ReportSchedule schedule = ReportSchedule.configure(
            targetId, 
            ReportSchedule.Frequency.WEEKLY, 
            AlertChannel.EMAIL
        );
        
        assertNotNull(schedule.id());
        assertEquals(targetId, schedule.targetId());
        assertEquals(ReportSchedule.Frequency.WEEKLY, schedule.frequency());
        assertEquals(AlertChannel.EMAIL, schedule.deliveryChannel());
    }

    @Test
    void rejectsNullDependencies() {
        assertThrows(NullPointerException.class, () -> 
            ReportSchedule.configure(null, ReportSchedule.Frequency.WEEKLY, AlertChannel.EMAIL)
        );
        assertThrows(NullPointerException.class, () -> 
            ReportSchedule.configure("target", null, AlertChannel.EMAIL)
        );
        assertThrows(NullPointerException.class, () -> 
            ReportSchedule.configure("target", ReportSchedule.Frequency.MONTHLY, null)
        );
    }
}
