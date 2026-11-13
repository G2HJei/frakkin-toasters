package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;

public class RevealCardsBeforeSkillCheckEventProcessor extends EventProcessor<RevealCardsBeforeSkillCheckEvent> {

    @Override
    public Followup process() {
        ops.skillCheck().revealPlayedBefore();
        // The current player decides which of the conflicting cards stand, then the cards that stand take effect.
        val tiebreak = shouldTieBreak()
                ? single(new PlayerDecisionEvent<>(game.turn().currentPlayerNumber(), TiebreakCardsBeforeSkillCheckEvent.class))
                : NONE;
        return allOf(tiebreak, single(new ResolveCardsBeforeSkillCheckEvent()));
    }

    private boolean shouldTieBreak() {
        return !game.activeSkillCheck().playedBeforeConflicts().isEmpty();
    }
}
