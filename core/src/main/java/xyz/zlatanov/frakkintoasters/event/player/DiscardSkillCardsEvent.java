package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

// The player chooses Skill Cards to discard, e.g. "the Admiral discards 2 Skill Cards."
// Supports the DISCARD_EXACTLY_1 to DISCARD_EXACTLY_5 constraints.
public record DiscardSkillCardsEvent(int playerNumber, List<SkillCard> cards,
                                     EventConstraint discardLimit) implements PlayerEvent {

    public DiscardSkillCardsEvent(int playerNumber, List<SkillCard> cards) {
        this(playerNumber, cards, null);
    }
}
