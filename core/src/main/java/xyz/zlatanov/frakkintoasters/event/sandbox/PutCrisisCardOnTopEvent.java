package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

// Takes the Crisis Card out of the draw pile and puts it on top, so the next Crisis step draws it.
public record PutCrisisCardOnTopEvent(CrisisCard card) implements SandboxEvent {
}
