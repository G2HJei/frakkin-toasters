package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.POLITICALLY_ADROIT;

// Ellen Tigh's "Politically Adroit": ending her Movement step with another human, she may give him a Skill Card from
// her hand, then she draws 2 Skill Cards.
@Source(ability = POLITICALLY_ADROIT)
public record PoliticallyAdroitEvent(int playerNumber, int targetPlayerNumber, SkillCard card) implements PlayerEvent {
}
