package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class CultLeaderEventProcessor extends CharacterActionEventProcessor<CultLeaderEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.CULT_LEADER;
    }

    public static boolean canUse(GameView game, PlayerView player) {
        return game.players().stream().anyMatch(other -> atHisLocation(game, player, other)
                && (canGive(player, other) || canTake(player, other)));
    }

    @Override
    protected boolean isValidUse() {
        if (!isPlayer(event.targetPlayerNumber())) {
            return false;
        }
        val other = game.player(event.targetPlayerNumber());
        return atHisLocation(game, player, other) && (event.give() ? canGive(player, other) : canTake(player, other));
    }

    @Override
    protected Followup resolve() {
        if (event.give()) {
            ops.player(event.playerNumber()).giveMiracleToken(event.targetPlayerNumber());
        } else {
            ops.player(event.targetPlayerNumber()).giveMiracleToken(event.playerNumber());
        }
        return NONE;
    }

    private static boolean atHisLocation(GameView game, PlayerView player, PlayerView other) {
        val location = game.locate(player.character());
        return other.number() != player.number() && location.isPresent()
                && location.equals(game.locate(other.character()));
    }

    // A player can gain a token only with room for it, and never as a revealed Cylon (rules 4.3.1 and 4.3.2).
    private static boolean canGive(PlayerView player, PlayerView other) {
        return player.miracleTokens() > 0 && other.canGainMiracleToken();
    }

    private static boolean canTake(PlayerView player, PlayerView other) {
        return other.miracleTokens() > 0 && player.canGainMiracleToken();
    }
}
