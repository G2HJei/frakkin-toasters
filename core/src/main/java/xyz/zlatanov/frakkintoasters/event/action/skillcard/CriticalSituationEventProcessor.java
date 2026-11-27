package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.Actions;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CRITICAL_SITUATION;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;

public class CriticalSituationEventProcessor extends SkillCardEventProcessor<CriticalSituationEvent> {

    public static boolean canPlay(GameView game, PlayerView player) {
        return !criticalSituationOrExecutiveOrderPlayed(game) && Actions.hasAnAction(game, player);
    }

    public static boolean criticalSituationOrExecutiveOrderPlayed(GameView game) {
        return game.turn().playedSkillCards().stream()
                .anyMatch(type -> type == CRITICAL_SITUATION || type == EXECUTIVE_ORDER);
    }

    @Override
    public SkillCardType type() {
        return CRITICAL_SITUATION;
    }

    @Override
    protected boolean isValidPlay() {
        return canPlay(game, player);
    }

    // The action is offered as the game stands after the card left the hand.
    @Override
    protected Followup resolve() {
        return Actions.offerAction(game, player.number());
    }
}
