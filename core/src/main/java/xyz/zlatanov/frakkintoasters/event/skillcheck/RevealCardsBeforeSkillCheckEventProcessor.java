package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class RevealCardsBeforeSkillCheckEventProcessor extends EventProcessor<RevealCardsBeforeSkillCheckEvent> {

    @Override
    public Followup process() {
        ops.skillCheck().revealPlayedBefore();
        return shouldTieBreak() ?
                single(new PlayerDecisionEvent<>(game.turn().currentPlayerNumber(), TiebreakCardsBeforeSkillCheckEvent.class))
                : NONE;
    }

    private boolean shouldTieBreak() {
        return !game.activeSkillCheck().playedBeforeConflicts().isEmpty();
    }
}
