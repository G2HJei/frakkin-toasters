package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.EMOTIONALLY_FRAGILE;

// Anastasia "Dee" Dualla's "Emotionally Fragile": morale was reduced to 2 or less, so she is executed. A human Dee's
// execution costs no morale.
@Source(ability = EMOTIONALLY_FRAGILE)
public record EmotionallyFragileEvent(int playerNumber) implements PlayerEvent {
}
