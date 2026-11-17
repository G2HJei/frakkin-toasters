package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.track.JumpPreparation;

// Puts the fleet token on a space of the Jump Preparation track. It doesn't jump the fleet, the rules' next advance does.
public record SetJumpPreparationEvent(JumpPreparation position) implements SandboxEvent {
}
