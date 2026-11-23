package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.FAST_LEARNER;

// Dee's "Fast Learner" (once per game): before a skill check she takes the top 3 cards of a Skill deck of any color,
// then puts all of them into the check (FastLearnerIntoCheckEvent) or keeps them.
@Source(ability = FAST_LEARNER)
public record FastLearnerEvent(int playerNumber, SkillCardColor color) implements PlayerEvent {
}
