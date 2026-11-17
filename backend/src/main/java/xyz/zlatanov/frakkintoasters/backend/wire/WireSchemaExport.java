package xyz.zlatanov.frakkintoasters.backend.wire;

import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes the wire schema to the file of the argument: {@code ./gradlew :backend:exportSchema} writes
 * {@code frontend/src/wire/schema.json}, and {@code npm run gen} turns it into {@code types.ts}.
 */
public final class WireSchemaExport {

    private WireSchemaExport() {
    }

    public static void main(String[] args) throws IOException {
        Files.writeString(Path.of(args[0]), pretty());
    }

    public static String pretty() {
        return JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build()
                .writeValueAsString(WireSchema.json()) + "\n";
    }
}
