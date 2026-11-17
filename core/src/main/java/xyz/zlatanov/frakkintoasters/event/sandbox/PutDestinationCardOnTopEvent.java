package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;

// Takes the Destination Card out of the draw pile and puts it on top, so the next jump draws it first.
public record PutDestinationCardOnTopEvent(DestinationCard card) implements SandboxEvent {
}
