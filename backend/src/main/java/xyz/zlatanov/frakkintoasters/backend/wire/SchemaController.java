package xyz.zlatanov.frakkintoasters.backend.wire;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The wire schema, the same JSON as the frontend's {@code src/wire/schema.json}.
 */
@RestController
public class SchemaController {

    private final String schema = WireSchemaExport.pretty();

    @GetMapping(value = "/api/schema", produces = MediaType.APPLICATION_JSON_VALUE)
    public String schema() {
        return schema;
    }
}
