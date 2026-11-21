package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Anastasia "Dee" Dualla's "Emotionally Fragile": morale was reduced to 2 or less, so she is executed. A human Dee's
// execution costs no morale.
public record EmotionallyFragileEvent(int playerNumber) implements PlayerEvent {
}
