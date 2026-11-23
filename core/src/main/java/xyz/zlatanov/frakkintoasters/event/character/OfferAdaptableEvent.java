package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.ADAPTABLE;

// A Quorum Card's action was used: Tory Foster may use "Adaptable", if she still can then. Queued only in games where
// someone has the ability.
@Source(ability = ADAPTABLE)
public record OfferAdaptableEvent() implements Event {
}
