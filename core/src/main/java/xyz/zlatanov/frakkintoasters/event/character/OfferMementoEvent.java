package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MEMENTO;

// Population was just lost: Brendan "Hot Dog" Costanza is offered "Memento" if he can still use it this turn.
@Source(ability = MEMENTO)
public record OfferMementoEvent(int playerNumber) implements PlayerEvent {
}
