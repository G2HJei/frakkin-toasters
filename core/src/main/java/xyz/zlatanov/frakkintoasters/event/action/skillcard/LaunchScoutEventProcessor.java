package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_SCOUT;

public class LaunchScoutEventProcessor extends SkillCardEventProcessor<LaunchScoutEvent> {

    // A raptor can be risked only when one is in the "Reserves" (rule 13.5). An assault raptor is no raptor (rule
    // 11.3.2.1).
    public static boolean canRiskARaptor(GameView game) {
        return game.boards().galactica().reserves().stream().anyMatch(Raptor.class::isInstance);
    }

    @Override
    public SkillCardType type() {
        return LAUNCH_SCOUT;
    }

    @Override
    protected boolean isValidPlay() {
        return canRiskARaptor(game);
    }

    @Override
    protected Followup resolve() {
        return rollDie(new LaunchScoutResultEvent(player.number()));
    }
}
