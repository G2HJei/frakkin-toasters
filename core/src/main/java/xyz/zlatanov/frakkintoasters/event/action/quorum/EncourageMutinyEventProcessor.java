package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEventProcessor.canBecomeAdmiral;

public class EncourageMutinyEventProcessor extends QuorumCardEventProcessor<EncourageMutinyEvent> {

    // Only a player who could take the title (canBecomeAdmiral leaves the Admiral out).
    public static boolean canPlay(GameView game, PlayerView player) {
        return game.players().stream().anyMatch(other -> isTarget(game, player, other));
    }

    @Override
    public QuorumCard card() {
        return QuorumCard.ENCOURAGE_MUTINY;
    }

    @Override
    protected boolean isValidPlay() {
        return isPlayer(event.targetPlayerNumber()) && isTarget(game, player, game.player(event.targetPlayerNumber()));
    }

    @Override
    protected Followup resolve() {
        return rollDie(new EncourageMutinyResultEvent(event.targetPlayerNumber()));
    }

    private static boolean isTarget(GameView game, PlayerView player, PlayerView other) {
        return other.number() != player.number() && canBecomeAdmiral(game, other.character());
    }
}
