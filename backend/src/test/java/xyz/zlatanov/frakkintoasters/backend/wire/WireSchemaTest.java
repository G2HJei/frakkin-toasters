package xyz.zlatanov.frakkintoasters.backend.wire;

import lombok.val;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WireSchemaTest {

    private static final Path CHECKED_IN = Path.of(System.getProperty("projectDir"))
            .resolveSibling("frontend").resolve("src/wire/schema.json");

    @Test
    void theFrontendsSchemaIsTheExportedOne() throws IOException {
        val exported = WireSchema.json();
        val checkedIn = JournalCodec.parse(Files.readString(CHECKED_IN));

        assertEquals(exported, checkedIn,
                "frontend/src/wire/schema.json is stale: run ./gradlew :backend:exportSchema, then npm run gen");
    }

    // The event builder picks a player, a ship or a number for an int, by its role.
    @Test
    void everyIntHasARole() {
        val missing = new ArrayList<String>();
        val types = WireSchema.json().get("types");
        types.propertyNames().forEach(name -> {
            val components = types.get(name).get("components");
            if (components != null) {
                components.forEach(component -> collectMissingRoles(name + "." + component.get("name").stringValue(),
                        component.get("type"), missing));
            }
        });

        assertEquals(List.of(), missing, "Teach WireSchema.role these names");
    }

    @Test
    void theSchemaListsTheEventsTheirLocationsAndTheSocketMessages() {
        val schema = WireSchema.json();
        val armory = schema.get("types").get("ArmoryEvent");

        assertEquals("ARMORY", armory.get("location").stringValue());
        assertTrue(contains(armory.get("interfaces"), "LocationEvent"));
        assertTrue(contains(schema.get("lists").get("sandboxEvents"), "PutCrisisCardOnTopEvent"));
        assertTrue(contains(schema.get("lists").get("anytimeEvents"), "RevealMotiveCardEvent"));
        assertTrue(contains(schema.get("types").get("AnytimeEvent").get("members"), "RevealMotiveCardEvent"));
        assertTrue(contains(schema.get("types").get("Event").get("members"), "MoveEvent"));
        assertEquals("GameMessage", schema.get("messages").get("game").stringValue());
        assertEquals("type", schema.get("types").get("ServerMessage").get("discriminator").stringValue());
    }

    private static void collectMissingRoles(String path, JsonNode type, List<String> missing) {
        if (type == null || !type.isObject()) {
            return;
        }
        if (type.get("t").stringValue().equals("int") && type.get("role").isNull()) {
            missing.add(path);
        }
        for (val nested : List.of("of", "key", "value")) {
            collectMissingRoles(path, type.get(nested), missing);
        }
    }

    private static boolean contains(JsonNode array, String value) {
        for (val element : array) {
            if (element.stringValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
