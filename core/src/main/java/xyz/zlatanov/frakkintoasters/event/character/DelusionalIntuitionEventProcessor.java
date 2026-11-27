package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Arrays;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_1_OF_ANY_COLOR;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.DELUSIONAL_INTUITION;

public class DelusionalIntuitionEventProcessor extends EventProcessor<DelusionalIntuitionEvent> {

    /**
     * A card has to be left to draw, so the draw has an answer.
     */
    public static boolean canUse(GameView game, PlayerView player) {
        return player.hasAbility(DELUSIONAL_INTUITION)
                && Arrays.stream(SkillCardColor.values()).anyMatch(color -> game.decks().skillCardsLeft(color) > 0);
    }

    @Override
    protected boolean isValid() {
        return canUse(game, player);
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(DELUSIONAL_INTUITION);
        return single(new PlayerDecisionEvent<>(player.number(), DrawSkillCardsEvent.class, DRAW_EXACTLY_1_OF_ANY_COLOR));
    }
}
