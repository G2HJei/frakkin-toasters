package xyz.zlatanov.frakkintoasters.event.player;

import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

@Accessors(fluent = true)
public record MoveEvent(int playerNumber, Location destination, SkillCard discardCard) implements PlayerEvent {
}
