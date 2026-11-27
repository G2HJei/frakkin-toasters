package xyz.zlatanov.frakkintoasters.backend.user;

import tools.jackson.databind.JsonNode;

import java.util.UUID;

/**
 * A login through a provider finds the user by the provider's subject: nothing else of the provider's profile is stored.
 */
public record AppUser(UUID id, String name, JsonNode settings) {
}
