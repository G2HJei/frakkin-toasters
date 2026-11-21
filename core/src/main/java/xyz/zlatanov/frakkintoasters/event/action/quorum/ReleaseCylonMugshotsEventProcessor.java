package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.LookAtLoyaltyCardEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

public class ReleaseCylonMugshotsEventProcessor extends QuorumCardEventProcessor<ReleaseCylonMugshotsEvent> {

    public static boolean canPlay(GameView game, PlayerView player) {
        return game.players().stream().anyMatch(other -> hasLoyaltyCardToLookAt(player, other));
    }

    @Override
    public QuorumCard card() {
        return QuorumCard.RELEASE_CYLON_MUGSHOTS;
    }

    @Override
    protected boolean isValidPlay() {
        return isPlayer(event.targetPlayerNumber())
                && hasLoyaltyCardToLookAt(player, game.player(event.targetPlayerNumber()));
    }

    // "Detector Sabotage" stops the look, not the roll.
    @Override
    protected Followup resolve() {
        return all(single(new LookAtLoyaltyCardEvent(player.number(), event.targetPlayerNumber())),
                rollDie(new ReleaseCylonMugshotsResultEvent()));
    }

    private static boolean hasLoyaltyCardToLookAt(PlayerView player, PlayerView other) {
        return other.number() != player.number() && !other.loyaltyCards().cards().isEmpty();
    }
}
