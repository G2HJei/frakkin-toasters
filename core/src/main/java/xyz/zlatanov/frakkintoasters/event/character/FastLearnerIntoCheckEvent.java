package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

// "Fast Learner": the cards she just took go into the skill check, face down.
public record FastLearnerIntoCheckEvent(int playerNumber, List<SkillCard> cards) implements PlayerEvent {
}
