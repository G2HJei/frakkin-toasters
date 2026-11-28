package xyz.zlatanov.frakkintoasters.backend.user;

import tools.jackson.databind.JsonNode;

import java.util.UUID;

/**
 * A login through a provider finds the user by the provider's subject or by its verified email: nothing else of the
 * provider's profile is stored. The email is null for the {@code dev} profile's users.
 */
public record AppUser(UUID id, String name, String email, JsonNode settings) {
}
