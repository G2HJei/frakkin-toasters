package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.MoveFromBrigEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.player.MoveFromBrigEventProcessor.isValidDestination;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

public class PresidentialPardonEventProcessor extends QuorumCardEventProcessor<PresidentialPardonEvent> {

    public static boolean canPlay(GameView game, PlayerView player) {
        return game.players().stream().anyMatch(other -> isOtherPrisoner(game, player, other));
    }

    @Override
    public QuorumCard card() {
        return QuorumCard.PRESIDENTIAL_PARDON;
    }

    @Override
    protected boolean isValidPlay() {
        return isPlayer(event.targetPlayerNumber())
                && isOtherPrisoner(game, player, game.player(event.targetPlayerNumber()))
                && isValidDestination(event.destination());
    }

    @Override
    protected Followup resolve() {
        return all(new MoveFromBrigEvent(event.targetPlayerNumber(), event.destination()), thenDiscard());
    }

    private static boolean isOtherPrisoner(GameView game, PlayerView player, PlayerView other) {
        return other.number() != player.number()
                && game.boards().galactica().charactersIn(BRIG).contains(other.character());
    }
}
