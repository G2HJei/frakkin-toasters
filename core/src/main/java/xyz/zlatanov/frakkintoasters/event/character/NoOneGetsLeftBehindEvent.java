package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.NO_ONE_GETS_LEFT_BEHIND;

// Karl "Helo" Agathon (alt.)'s "No One Gets Left Behind" (once per game): during the ships' removal of a jump, 1 fuel is
// lost and 2 population gained.
@Source(ability = NO_ONE_GETS_LEFT_BEHIND)
public record NoOneGetsLeftBehindEvent(int playerNumber) implements PlayerEvent {
}
