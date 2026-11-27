package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.POLITICALLY_ADROIT;

public class PoliticallyAdroitEventProcessor extends EventProcessor<PoliticallyAdroitEvent> {

    public static boolean canGive(GameView game, PlayerView player, PlayerView target) {
        val location = game.locate(player.character());
        return player.hasAbility(POLITICALLY_ADROIT)
                && !player.skillCards().isEmpty()
                && target.number() != player.number()
                && target.character() != null
                && target.isHuman()
                && location.isPresent()
                && game.locate(target.character()).equals(location);
    }

    public static boolean canGiveToAnyone(GameView game, PlayerView player) {
        return game.players().stream().anyMatch(other -> canGive(game, player, other));
    }

    @Override
    protected boolean isValid() {
        return isPlayer(event.targetPlayerNumber())
                && canGive(game, player, game.player(event.targetPlayerNumber()))
                && event.card() != null
                && holdsSkillCards(List.of(event.card()));
    }

    @Override
    public Followup process() {
        val playerOps = ops.player(player.number());
        playerOps.useAbility(POLITICALLY_ADROIT);
        playerOps.giveSkillCards(event.targetPlayerNumber(), List.of(event.card()));
        // She draws the 2 cards from her own skill set.
        return single(new PlayerDecisionEvent<>(player.number(), DrawSkillCardsEvent.class, DRAW_EXACTLY_2));
    }
}
