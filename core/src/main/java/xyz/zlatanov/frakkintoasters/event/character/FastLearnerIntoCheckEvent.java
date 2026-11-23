package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.FAST_LEARNER;

// "Fast Learner": the cards she just took go into the skill check, face down.
@Source(ability = FAST_LEARNER)
public record FastLearnerIntoCheckEvent(int playerNumber, List<SkillCard> cards) implements PlayerEvent {
}
