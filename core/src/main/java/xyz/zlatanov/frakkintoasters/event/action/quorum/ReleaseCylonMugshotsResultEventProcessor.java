package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.RELEASE_CYLON_MUGSHOTS;

public class ReleaseCylonMugshotsResultEventProcessor extends EventProcessor<ReleaseCylonMugshotsResultEvent> {

    private static final int HIGHEST_ROLL_TO_LOSE = 3;

    @Override
    public Followup process() {
        ops.titles().discardPlayedQuorumCard(RELEASE_CYLON_MUGSHOTS);
        return game.turn().dieRoll().result() <= HIGHEST_ROLL_TO_LOSE ? single(lose(MORALE, 1)) : NONE;
    }
}
