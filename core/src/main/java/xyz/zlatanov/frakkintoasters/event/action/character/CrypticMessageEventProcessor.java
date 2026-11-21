package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class CrypticMessageEventProcessor extends CharacterActionEventProcessor<CrypticMessageEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.CRYPTIC_MESSAGE;
    }

    public static boolean canUse(GameView game, PlayerView player) {
        return game.players().stream().anyMatch(other -> isTarget(player, other));
    }

    @Override
    protected boolean isValidUse() {
        return isPlayer(event.targetPlayerNumber()) && isTarget(player, game.player(event.targetPlayerNumber()));
    }

    @Override
    protected Followup resolve() {
        val his = player.skillCards().cards();
        val theirs = game.player(event.targetPlayerNumber()).skillCards().cards();
        ops.player(event.playerNumber()).giveSkillCards(event.targetPlayerNumber(), his);
        ops.player(event.targetPlayerNumber()).giveSkillCards(event.playerNumber(), theirs);
        return NONE;
    }

    private static boolean isTarget(PlayerView player, PlayerView other) {
        return other.number() != player.number() && other.character() != null;
    }
}
