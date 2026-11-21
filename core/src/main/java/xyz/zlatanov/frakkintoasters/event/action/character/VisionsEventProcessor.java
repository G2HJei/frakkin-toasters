package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.LookAtLoyaltyCardEvent;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.DETECTOR_SABOTAGE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class VisionsEventProcessor extends CharacterActionEventProcessor<VisionsEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.VISIONS;
    }

    // Not while "Detector Sabotage" forbids looking at Loyalty Cards, as "Cylon Detector".
    public static boolean canUse(GameView game, PlayerView player) {
        return player.isInfiltrating()
                && !game.crisisCardsInPlay().contains(DETECTOR_SABOTAGE)
                && game.players().stream().anyMatch(other -> isTarget(player, other));
    }

    @Override
    protected boolean isValidUse() {
        return isPlayer(event.targetPlayerNumber()) && canUse(game, player)
                && isTarget(player, game.player(event.targetPlayerNumber()));
    }

    // The look comes first: a Final Five card it finds may execute her while she is still Infiltrating.
    @Override
    protected Followup resolve() {
        return all(new LookAtLoyaltyCardEvent(event.playerNumber(), event.targetPlayerNumber()),
                new EndInfiltrationEvent(event.playerNumber()));
    }

    // A human with a Loyalty Card to look at: an Infiltrating Cylon Leader has none.
    private static boolean isTarget(PlayerView player, PlayerView other) {
        return other.number() != player.number() && other.isHuman() && !other.loyaltyCards().cards().isEmpty();
    }
}
