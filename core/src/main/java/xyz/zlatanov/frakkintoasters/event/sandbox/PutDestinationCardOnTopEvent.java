package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;

public record PutDestinationCardOnTopEvent(DestinationCard card) implements SandboxEvent {
}
