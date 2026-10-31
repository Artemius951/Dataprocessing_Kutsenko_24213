package ru.nsu.kutsenko.J3;

import java.util.List;

public record ServerResponse(
    String message,
    List<String> successors
) {
}