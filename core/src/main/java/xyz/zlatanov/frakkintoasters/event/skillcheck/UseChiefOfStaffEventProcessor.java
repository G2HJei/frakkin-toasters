package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ASSIGN_CHIEF_OF_STAFF;

public class UseChiefOfStaffEventProcessor extends EventProcessor<UseChiefOfStaffEvent> {

    @Override
    protected boolean isValid() {
        return game.quorumCardHolder(ASSIGN_CHIEF_OF_STAFF).equals(Optional.of(event.playerNumber()))
                && game.activeSkillCheck() != null;
    }

    @Override
    public Followup process() {
        ops.skillCheck().makePoliticsCountPositive();
        ops.titles().discardFromPlay(ASSIGN_CHIEF_OF_STAFF);
        return Followup.NONE;
    }
}
