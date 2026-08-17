package com.mailsent.resolution.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ResolutionActionTest {

    @Test
    void createsResolutionAction() {
        ResolutionAction action = ResolutionAction.create(
            "Fix DNS", "Ensure SPF is valid.", List.of("Step 1", "Step 2"), 1
        );

        assertNotNull(action.id());
        assertEquals("Fix DNS", action.title());
        assertEquals("Ensure SPF is valid.", action.description());
        assertEquals(2, action.exactSteps().size());
        assertEquals("Step 1", action.exactSteps().get(0));
        assertEquals(1, action.priority());
    }

    @Test
    void exactStepsAreImmutable() {
        List<String> steps = new java.util.ArrayList<>();
        steps.add("Step 1");
        
        ResolutionAction action = ResolutionAction.create("Title", "Desc", steps, 1);
        
        steps.add("Step 2");
        assertEquals(1, action.exactSteps().size());
        
        assertThrows(UnsupportedOperationException.class, () -> action.exactSteps().add("Step 3"));
    }
}
