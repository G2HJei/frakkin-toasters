package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.LookAtAllLoyaltyCardsEvent;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.DETECTOR_SABOTAGE;

public class CylonDetectorEventProcessor extends CharacterActionEventProcessor<CylonDetectorEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.CYLON_DETECTOR;
    }

    // Not while "Detector Sabotage" forbids looking at Loyalty Cards: the look would find nothing.
    public static boolean canUse(GameView game, PlayerView player) {
        return !game.crisisCardsInPlay().contains(DETECTOR_SABOTAGE)
                && game.players().stream().anyMatch(other -> isTarget(player, other));
    }

    @Override
    protected boolean isValidUse() {
        return isPlayer(event.targetPlayerNumber()) && canUse(game, player)
                && isTarget(player, game.player(event.targetPlayerNumber()));
    }

    @Override
    protected Followup resolve() {
        return single(new LookAtAllLoyaltyCardsEvent(event.playerNumber(), event.targetPlayerNumber()));
    }

    private static boolean isTarget(PlayerView player, PlayerView other) {
        return other.number() != player.number() && !other.loyaltyCards().cards().isEmpty();
    }
}
