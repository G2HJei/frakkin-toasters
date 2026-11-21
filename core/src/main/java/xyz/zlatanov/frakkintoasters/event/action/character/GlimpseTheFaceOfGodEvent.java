package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterMovementEvent;

// Leoben Conoy's "Glimpse the Face of God" (Movement): he takes the top 2 cards of the Destiny deck into his hand, then
// puts 2 cards from his hand on top of it (GlimpseTheFaceOfGodPlaceEvent).
public record GlimpseTheFaceOfGodEvent(int playerNumber) implements CharacterMovementEvent {
}
