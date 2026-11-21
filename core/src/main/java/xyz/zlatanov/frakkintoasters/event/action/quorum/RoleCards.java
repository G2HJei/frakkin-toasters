package xyz.zlatanov.frakkintoasters.event.action.quorum;

import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static lombok.AccessLevel.PRIVATE;

// The Quorum Cards given to a player, who holds them in play: the four "Assign" cards and "Probation".
@NoArgsConstructor(access = PRIVATE)
final class RoleCards {

    // A human player, an Infiltrating Cylon Leader included (rule 6.2.3.3). Revealed Cylons lose them (rule 6.1.1).
    static boolean canHold(GameView game, Integer holderPlayerNumber) {
        return holderPlayerNumber != null
                && holderPlayerNumber >= 1 && holderPlayerNumber <= game.players().size()
                && game.player(holderPlayerNumber).isHuman();
    }

    // "Give this card to any other player"
    static boolean canHoldOther(GameView game, PlayerView player, Integer holderPlayerNumber) {
        return canHold(game, holderPlayerNumber) && holderPlayerNumber != player.number();
    }

    static boolean anyOther(GameView game, PlayerView player) {
        return game.players().stream().anyMatch(other -> canHoldOther(game, player, other.number()));
    }
}
