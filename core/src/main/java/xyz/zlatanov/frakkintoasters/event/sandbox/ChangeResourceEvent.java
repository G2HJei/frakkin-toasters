package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.board.Resource;

// Raises a resource by the amount, or lowers it by a negative one, within 0 and 15. At 0 the Cylons win at the end of the
// turn, as by the rules.
public record ChangeResourceEvent(Resource resource, int amount) implements SandboxEvent {
}
