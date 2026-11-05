package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

// Research Lab (Galactica location)
// Action: Draw 1 engineering or 1 tactics Skill Card.
public record ResearchLabEvent(int playerNumber, SkillCardColor skillCardColor) implements ActionEvent {

}
