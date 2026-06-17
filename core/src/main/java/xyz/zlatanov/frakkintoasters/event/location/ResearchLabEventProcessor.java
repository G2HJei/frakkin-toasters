package xyz.zlatanov.frakkintoasters.event.location;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.ENGINEERING;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TACTICS;

public class ResearchLabEventProcessor extends EventProcessor<ResearchLabEvent> {

    private static final List<SkillCardColor> ALLOWED_SKILL_CARD_COLORS = List.of(ENGINEERING, TACTICS);

    @Override
    protected boolean isValid() {
        return ALLOWED_SKILL_CARD_COLORS.contains(event.skillCardColor());
    }

    @Override
    public Followup process() {
        game.drawSkillCard(event.playerNumber(), event.skillCardColor());
        return Followup.NONE;
    }
}
