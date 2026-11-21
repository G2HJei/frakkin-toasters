package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

// Launch Scout - color: TACTICS, values: 1, 2
// Action: risk a raptor and roll a die. On 3 or higher, look at the top card of the Crisis or the Destination deck and
// put it on the top or the bottom. Otherwise destroy 1 raptor.
public record LaunchScoutEvent(int playerNumber, SkillCard card) implements SkillCardEvent, ActionEvent {
}
