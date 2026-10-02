package ru.keyserver;

public final class GenerationResult {
    private final String name;
    private final KeyMaterial material;
    private final Throwable error;

    private GenerationResult(
        String name,
        KeyMaterial material,
        Throwable error) {
        this.name = name;
        this.material = material;
        this.error = error;
    }

    public static GenerationResult success(
        String name,
        KeyMaterial material) {
        return new GenerationResult(
            name,
            material,
            null);
    }

    public static GenerationResult failure(
        String name,
        Throwable error) {
        return new GenerationResult(
            name,
            null,
            error);
    }

    public String getName() {
        return name;
    }

    public KeyMaterial getMaterial() {
        return material;
    }

    public Throwable getError() {
        return error;
    }

    public boolean isSuccess() {
        return error == null;
    }
}