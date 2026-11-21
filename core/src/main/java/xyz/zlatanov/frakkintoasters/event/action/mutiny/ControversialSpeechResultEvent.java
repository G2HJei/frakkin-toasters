package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Event;

// "Controversial Speech", after the die roll: on 6 or more +1 morale and the card leaves the game, otherwise it is
// discarded and every player draws 1 Treachery Card.
public record ControversialSpeechResultEvent() implements Event {
}
