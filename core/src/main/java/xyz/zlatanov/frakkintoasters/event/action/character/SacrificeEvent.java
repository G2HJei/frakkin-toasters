package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.SACRIFICE;

// Louanne "Kat" Katraine's "Sacrifice", once per game, while piloting: she goes to "Sickbay" to destroy in her
// space area 5 raiders, 2 heavy raiders, 1 basestar or 1 civilian ship (shipIds; all of them when fewer are
// there).
@Source(ability = SACRIFICE)
public record SacrificeEvent(int playerNumber, List<Integer> shipIds) implements CharacterActionEvent {
}
