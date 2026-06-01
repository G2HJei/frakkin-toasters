package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

public record EngineRoomEvent(int playerNumber, SkillCard discardCard1, SkillCard discardCard2) implements ActionEvent {
}
