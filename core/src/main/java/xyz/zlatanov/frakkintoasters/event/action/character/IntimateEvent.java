package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterMovementEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.INTIMATE;

// "Caprica" Six's "Intimate" (Movement): she takes a random Skill Card from another player's hand, then that player
// draws a card from the Skill deck she names, whatever his skill set.
@Source(ability = INTIMATE)
public record IntimateEvent(int playerNumber, int targetPlayerNumber, SkillCardColor deck) implements CharacterMovementEvent {
}
