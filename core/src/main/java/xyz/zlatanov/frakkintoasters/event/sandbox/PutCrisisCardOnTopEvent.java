package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

public record PutCrisisCardOnTopEvent(CrisisCard card) implements SandboxEvent {
}
