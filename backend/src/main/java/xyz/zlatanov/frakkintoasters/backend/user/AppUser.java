package xyz.zlatanov.frakkintoasters.backend.user;

import tools.jackson.databind.JsonNode;

import java.util.UUID;

/**
 * A user of the site. A login through a provider finds him by the provider's subject, nothing else of the provider's
 * profile is stored.
 *
 * @param settings what the frontend keeps for the user, as it likes
 */
public record AppUser(UUID id, String name, JsonNode settings) {
}
