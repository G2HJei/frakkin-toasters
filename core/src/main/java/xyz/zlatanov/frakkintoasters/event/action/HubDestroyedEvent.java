package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Hub Destroyed (Cylon location, Daybreak "Hub Destroyed" overlay, replaces Resurrection Ship)
// During your Draw Skills step, discard all of your Super Crisis Cards and do not draw Skill Cards.
// Action: Discard 3 Skill Cards to draw 1 Super Crisis Card and move to the "Cylon Fleet" location.
public record HubDestroyedEvent(int playerNumber, SkillCard discardCard1, SkillCard discardCard2,
                                SkillCard discardCard3) implements ActionEvent {
}
