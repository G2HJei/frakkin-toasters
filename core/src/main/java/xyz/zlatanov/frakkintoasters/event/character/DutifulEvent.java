package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Louis Hoshi's "Dutiful" (once during his turn): right after he activates "Command", "Communications" or "Weapons
// Control", he discards a Skill Card to activate the same location again.
public record DutifulEvent(int playerNumber, Location location, SkillCard card) implements PlayerEvent {
}
