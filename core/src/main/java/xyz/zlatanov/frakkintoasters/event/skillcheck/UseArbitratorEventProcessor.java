package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ASSIGN_ARBITRATOR;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

public class UseArbitratorEventProcessor extends EventProcessor<UseArbitratorEvent> {

    static final int ADJUSTMENT = 3;

    @Override
    protected boolean isValid() {
        return Math.abs(event.adjustment()) == ADJUSTMENT
                && game.quorumCardHolder(ASSIGN_ARBITRATOR).equals(Optional.of(event.playerNumber()))
                && game.activeSkillCheck() != null
                && game.activeSkillCheck().type() == ADMIRALS_QUARTERS;
    }

    @Override
    public Followup process() {
        ops.skillCheck().adjustDifficulty(event.adjustment());
        ops.titles().discardFromPlay(ASSIGN_ARBITRATOR);
        return Followup.NONE;
    }
}
