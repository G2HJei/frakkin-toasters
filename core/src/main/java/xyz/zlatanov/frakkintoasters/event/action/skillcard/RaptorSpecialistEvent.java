package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Raptor Specialist - color: ENGINEERING, values: 3, 4
// Action: destroy a raptor to gain an assault raptor, or return a destroyed raptor to the "Reserves".
public record RaptorSpecialistEvent(int playerNumber, SkillCard card, Choice choice)
        implements SkillCardEvent, ActionEvent {

    public enum Choice {
        BUILD_ASSAULT_RAPTOR,
        RETURN_RAPTOR
    }
}
