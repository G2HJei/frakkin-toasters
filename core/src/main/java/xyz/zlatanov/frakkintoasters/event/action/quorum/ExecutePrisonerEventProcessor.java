package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.player.ExecuteEventProcessor.canBeExecuted;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

public class ExecutePrisonerEventProcessor extends QuorumCardEventProcessor<ExecutePrisonerEvent> {

    public static boolean canPlay(GameView game) {
        return game.players().stream().map(PlayerView::number).anyMatch(n -> isPrisoner(game, n));
    }

    @Override
    public QuorumCard card() {
        return QuorumCard.EXECUTE_PRISONER;
    }

    @Override
    protected boolean isValidPlay() {
        return isPlayer(event.targetPlayerNumber()) && isPrisoner(game, event.targetPlayerNumber());
    }

    // Discarded before the execution, so no card waits outside a pile while a new character is chosen. Nothing can tell
    // the difference.
    @Override
    protected Followup resolve() {
        ops.titles().discardPlayedQuorumCard(card());
        return single(new ExecuteEvent(event.targetPlayerNumber()));
    }

    private static boolean isPrisoner(GameView game, int playerNumber) {
        return game.boards().galactica().charactersIn(BRIG).contains(game.player(playerNumber).character())
                && canBeExecuted(game, playerNumber);
    }
}
