package ru.keyserver;

public final class GenerationRequest {
    private final String name;

    public GenerationRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}