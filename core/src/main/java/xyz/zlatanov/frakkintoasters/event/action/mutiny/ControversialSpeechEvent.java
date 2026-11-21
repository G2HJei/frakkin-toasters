package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// Controversial Speech (Mutiny Card)
// Action: roll a die. On 6 or more, +1 morale and the card leaves the game. Otherwise discard it, and every player,
// Cylons included, draws 1 Treachery Card.
public record ControversialSpeechEvent(int playerNumber) implements MutinyCardEvent {
}
