package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.AUTHORIZATION_OF_BRUTAL_FORCE;

public class AuthorizationOfBrutalForceResultEventProcessor extends EventProcessor<AuthorizationOfBrutalForceResultEvent> {

    private static final int HIGHEST_ROLL_TO_LOSE = 2;

    @Override
    public Followup process() {
        ops.titles().discardPlayedQuorumCard(AUTHORIZATION_OF_BRUTAL_FORCE);
        return game.turn().dieRoll().result() <= HIGHEST_ROLL_TO_LOSE ? single(lose(POPULATION, 1)) : NONE;
    }
}
