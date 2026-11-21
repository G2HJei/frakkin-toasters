package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class ProbationEventProcessor extends QuorumCardEventProcessor<ProbationEvent> {

    // "Any player", the President too.
    public static boolean canPlay(GameView game) {
        return game.players().stream().anyMatch(other -> RoleCards.canHold(game, other.number()));
    }

    @Override
    public QuorumCard card() {
        return QuorumCard.PROBATION;
    }

    @Override
    protected boolean isValidPlay() {
        return RoleCards.canHold(game, event.holderPlayerNumber());
    }

    @Override
    protected Followup resolve() {
        ops.titles().putInPlay(card(), event.holderPlayerNumber());
        return NONE;
    }
}
