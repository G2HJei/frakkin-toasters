package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterMovementEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

// "Caprica" Six's "Intimate" (Movement): she takes a random Skill Card from another player's hand, then that player
// draws a card from the Skill deck she names, whatever his skill set.
public record IntimateEvent(int playerNumber, int targetPlayerNumber, SkillCardColor deck) implements CharacterMovementEvent {
}
