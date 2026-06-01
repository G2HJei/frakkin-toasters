package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

public record HubDestroyedEvent(int playerNumber, SkillCard discardCard1, SkillCard discardCard2,
                                SkillCard discardCard3) implements ActionEvent {
}
