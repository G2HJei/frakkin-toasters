package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.SLEEPER_AGENT;

// Sharon "Boomer" Valerii's "Sleeper Agent": after her two Loyalty Cards of the Sleeper Agent phase, she moves to the
// "Brig".
@Source(ability = SLEEPER_AGENT)
public record SleeperAgentEvent(int playerNumber) implements PlayerEvent {
}
