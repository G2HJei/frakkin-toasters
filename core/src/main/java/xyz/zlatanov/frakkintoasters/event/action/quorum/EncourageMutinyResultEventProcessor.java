package xyz.zlatanov.frakkintoasters.event.action.quorum;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEventProcessor.canBecomeAdmiral;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ENCOURAGE_MUTINY;

public class EncourageMutinyResultEventProcessor extends EventProcessor<EncourageMutinyResultEvent> {

    private static final int LOWEST_ROLL_TO_BECOME_ADMIRAL = 3;

    @Override
    protected boolean isValid() {
        return isPlayer(event.targetPlayerNumber());
    }

    @Override
    public Followup process() {
        ops.titles().discardPlayedQuorumCard(ENCOURAGE_MUTINY);
        if (game.turn().dieRoll().result() < LOWEST_ROLL_TO_BECOME_ADMIRAL) {
            return single(lose(MORALE, 1));
        }
        val character = game.player(event.targetPlayerNumber()).character();
        return canBecomeAdmiral(game, character) ? single(new MakeAdmiralEvent(character)) : NONE;
    }
}
