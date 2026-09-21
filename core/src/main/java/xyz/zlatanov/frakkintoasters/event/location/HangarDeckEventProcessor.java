package xyz.zlatanov.frakkintoasters.event.location;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillSetOption;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.MUST_PILOT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.PILOTING;

public class HangarDeckEventProcessor extends EventProcessor<HangarDeckEvent> {

    @Override
    protected boolean isValid() {
        return player.character().skillSet()
                .stream()
                .map(SkillSetOption::colors)
                .anyMatch(colors -> colors.contains(PILOTING));
    }

    @Override
    public Followup process() {
        return all(
                single(
                        new PlayerDecisionEvent<>(player.number(), LaunchViperEvent.class, MUST_PILOT)),
                one(
                        new PlayerDecisionEvent<>(player.number(), ActionEvent.class),
                        new NoOpEvent(player.number())));
    }

}
