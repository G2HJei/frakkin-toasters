package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Simon O'Neill's "Logic-Bound": one of the cards he just put into a skill check goes in face up.
public record LogicBoundEvent(int playerNumber, SkillCard card) implements PlayerEvent {
}
