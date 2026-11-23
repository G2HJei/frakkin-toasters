package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.KLEPTOMANIA;

// Romo Lampkin's "Kleptomania": he couldn't discard 2 Skill Cards after ending his Movement step with another player,
// so he goes to the "Brig" at the end of his turn. The Discard step queues it.
@Source(ability = KLEPTOMANIA)
public record KleptomaniaBrigEvent(int playerNumber) implements PlayerEvent {
}
