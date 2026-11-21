package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.event.action.skillcard.LaunchScoutEventProcessor.canRiskARaptor;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.SCOUTING_FOR_FUEL;

public class ScoutForFuelEventProcessor extends SkillCardEventProcessor<ScoutForFuelEvent> {

    @Override
    public SkillCardType type() {
        return SCOUTING_FOR_FUEL;
    }

    @Override
    protected boolean isValidPlay() {
        return canRiskARaptor(game);
    }

    @Override
    protected Followup resolve() {
        return rollDie(new ScoutForFuelResultEvent());
    }
}
