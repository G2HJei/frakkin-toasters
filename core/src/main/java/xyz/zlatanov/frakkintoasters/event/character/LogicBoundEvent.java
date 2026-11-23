package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.LOGIC_BOUND;

// Simon O'Neill's "Logic-Bound": one of the cards he just put into a skill check goes in face up.
@Source(ability = LOGIC_BOUND)
public record LogicBoundEvent(int playerNumber, SkillCard card) implements PlayerEvent {
}
