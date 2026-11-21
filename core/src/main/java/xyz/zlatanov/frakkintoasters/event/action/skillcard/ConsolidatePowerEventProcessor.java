package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.Arrays;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2_OF_ANY_COLOR;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;

public class ConsolidatePowerEventProcessor extends SkillCardEventProcessor<ConsolidatePowerEvent> {

    public static boolean canPlay(GameView game) {
        return Arrays.stream(SkillCardColor.values()).anyMatch(color -> game.decks().skillCardsLeft(color) > 0);
    }

    @Override
    public SkillCardType type() {
        return CONSOLIDATE_POWER;
    }

    @Override
    protected boolean isValidPlay() {
        return canPlay(game);
    }

    @Override
    protected Followup resolve() {
        // "Any type" includes Treachery, as for "Consult the Hybrid".
        return single(new PlayerDecisionEvent<>(player.number(), DrawSkillCardsEvent.class, DRAW_EXACTLY_2_OF_ANY_COLOR));
    }
}
