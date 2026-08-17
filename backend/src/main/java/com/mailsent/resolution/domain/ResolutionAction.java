package com.mailsent.resolution.domain;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ResolutionAction {
    private final String id;
    private final String title;
    private final String description;
    private final List<String> exactSteps;
    private final int priority;

    public ResolutionAction(String id, String title, String description, List<String> exactSteps, int priority) {
        this.id = Objects.requireNonNull(id, "ID is required");
        this.title = Objects.requireNonNull(title, "Title is required");
        this.description = Objects.requireNonNull(description, "Description is required");
        this.exactSteps = List.copyOf(Objects.requireNonNull(exactSteps, "Exact steps are required"));
        this.priority = priority;
    }

    public static ResolutionAction create(String title, String description, List<String> exactSteps, int priority) {
        return new ResolutionAction(UUID.randomUUID().toString(), title, description, exactSteps, priority);
    }

    public String id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public List<String> exactSteps() { return exactSteps; }
    public int priority() { return priority; }
}
