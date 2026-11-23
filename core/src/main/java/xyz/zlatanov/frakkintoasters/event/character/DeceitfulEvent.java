package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.player.DiscardKind;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.DECEITFUL;

// Romo Lampkin's "Deceitful": a discard a Crisis Card makes him do is 1 card smaller, once per card. The count is the
// card's, the reduction is worked out when the discard happens, from the hand he holds then.
@Source(ability = DECEITFUL)
public record DeceitfulEvent(int playerNumber, DiscardKind discard, int count) implements PlayerEvent {
}
