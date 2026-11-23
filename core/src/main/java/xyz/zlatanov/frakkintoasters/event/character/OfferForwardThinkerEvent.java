package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.FORWARD_THINKER;

// Lee Adama's "Executive Order" is over: he is offered "Forward Thinker" if he can still use it then. Queued only when
// he has the ability.
@Source(ability = FORWARD_THINKER)
public record OfferForwardThinkerEvent(int playerNumber) implements PlayerEvent {
}
