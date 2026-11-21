package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;

public class EulogyEventProcessor extends QuorumCardEventProcessor<EulogyEvent> {

    // Any execution earlier in the game counts.
    public static boolean canPlay(GameView game) {
        return game.moraleLostToExecution();
    }

    @Override
    public QuorumCard card() {
        return QuorumCard.EULOGY;
    }

    @Override
    protected boolean isValidPlay() {
        return canPlay(game);
    }

    @Override
    protected Followup resolve() {
        return all(gain(MORALE, 1), thenDiscard());
    }
}
