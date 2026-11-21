package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.FOOD_RATIONING;

public class FoodRationingResultEventProcessor extends EventProcessor<FoodRationingResultEvent> {

    private static final int LOWEST_ROLL_TO_GAIN = 6;

    @Override
    public Followup process() {
        return gainOnSix(game, ops, FOOD_RATIONING, FOOD);
    }

    // "Food Rationing" and "Inspirational Speech" differ only in the resource.
    static Followup gainOnSix(GameView game, GameOps ops, QuorumCard card, Resource resource) {
        if (game.turn().dieRoll().result() < LOWEST_ROLL_TO_GAIN) {
            ops.titles().discardPlayedQuorumCard(card);
            return NONE;
        }
        ops.titles().removeQuorumCardFromGame(card);
        return single(gain(resource, 1));
    }
}
