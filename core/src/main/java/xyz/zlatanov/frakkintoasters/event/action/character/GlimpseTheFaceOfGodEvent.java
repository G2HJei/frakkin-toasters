package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterMovementEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.GLIMPSE_THE_FACE_OF_GOD;

// Leoben Conoy's "Glimpse the Face of God" (Movement): he takes the top 2 cards of the Destiny deck into his hand, then
// puts 2 cards from his hand on top of it (GlimpseTheFaceOfGodPlaceEvent).
@Source(ability = GLIMPSE_THE_FACE_OF_GOD)
public record GlimpseTheFaceOfGodEvent(int playerNumber) implements CharacterMovementEvent {
}
