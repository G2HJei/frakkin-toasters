package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.player.SendToBrigEventProcessor.canBeSentToBrig;

public class ArrestOrderEventProcessor extends QuorumCardEventProcessor<ArrestOrderEvent> {

    // Any character, the President's own too.
    public static boolean canPlay(GameView game) {
        return game.players().stream().map(PlayerView::number).anyMatch(n -> canBeSentToBrig(game, n));
    }

    @Override
    public QuorumCard card() {
        return QuorumCard.ARREST_ORDER;
    }

    @Override
    protected boolean isValidPlay() {
        return isPlayer(event.targetPlayerNumber()) && canBeSentToBrig(game, event.targetPlayerNumber());
    }

    @Override
    protected Followup resolve() {
        return all(new SendToBrigEvent(event.targetPlayerNumber()), thenDiscard());
    }
}
