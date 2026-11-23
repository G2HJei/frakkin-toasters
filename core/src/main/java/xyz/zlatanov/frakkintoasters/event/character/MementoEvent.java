package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MEMENTO;

// Brendan "Hot Dog" Costanza's "Memento" (once per turn): right after population is lost, he draws the top 3 Piloting
// cards, then discards 1 of them and keeps the other 2.
@Source(ability = MEMENTO)
public record MementoEvent(int playerNumber) implements PlayerEvent {
}
