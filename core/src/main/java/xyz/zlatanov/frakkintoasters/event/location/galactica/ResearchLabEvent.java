package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import static xyz.zlatanov.frakkintoasters.state.board.Location.RESEARCH_LAB;

// Research Lab (Galactica location)
// Action: Draw 1 engineering or 1 tactics Skill Card.
public record ResearchLabEvent(int playerNumber, SkillCardColor skillCardColor) implements LocationEvent {

    @Override
    public Location location() {
        return RESEARCH_LAB;
    }
}
