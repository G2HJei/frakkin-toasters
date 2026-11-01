package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Force Their Hand - color: POLITICS, values: 0
// Skill Check: If the current player is a human player, he may play 1 Skill Card faceup into this check. If he does
// not, he must draw 1 Mutiny Card.
public record ForceTheirHandEvent(int playerNumber, SkillCard skillCard) implements PlayerEvent {
}
