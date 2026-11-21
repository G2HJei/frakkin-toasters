package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEventProcessor;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class DischargeOfAFirearmEventProcessor extends CharacterActionEventProcessor<DischargeOfAFirearmEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.DISCHARGE_OF_A_FIREARM;
    }

    public static boolean canUse(GameView game, PlayerView player) {
        return game.players().stream().anyMatch(other -> isTarget(game, player, other.number()));
    }

    @Override
    protected boolean isValidUse() {
        return isPlayer(event.targetPlayerNumber()) && isTarget(game, player, event.targetPlayerNumber());
    }

    @Override
    protected Followup resolve() {
        return single(new ExecuteEvent(event.targetPlayerNumber()));
    }

    // Another character at her location, who can be executed: a revealed Cylon ignores it.
    private static boolean isTarget(GameView game, PlayerView player, int targetPlayerNumber) {
        val location = game.locate(player.character());
        return targetPlayerNumber != player.number()
                && location.isPresent()
                && location.equals(game.locate(game.player(targetPlayerNumber).character()))
                && ExecuteEventProcessor.canBeExecuted(game, targetPlayerNumber);
    }
}
