package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.event.action.ResetActingPlayerEvent;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.MOVE_OR_ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.STATE_OF_EMERGENCY;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class StateOfEmergencyEventProcessorTest extends EventTestHarness<StateOfEmergencyEvent> {

    private static final SkillCard STATE_OF_EMERGENCY_6 = new SkillCard(6, SkillCardType.STATE_OF_EMERGENCY);

    // Each player, starting with the one who played it and going clockwise.
    @Test
    void loseFoodThenEachPlayerMayMoveOrAct() {
        player(2).character(LAURA_ROSLIN);
        player(2).gainSkillCards(STATE_OF_EMERGENCY_6);

        execute(new StateOfEmergencyEvent(2, STATE_OF_EMERGENCY_6));

        assertFollowup(all(
                new LoseResourceEvent(FOOD, 1),
                new GrantEvent(2, MOVE_OR_ACTION, STATE_OF_EMERGENCY),
                new GrantEvent(3, MOVE_OR_ACTION, STATE_OF_EMERGENCY),
                new GrantEvent(1, MOVE_OR_ACTION, STATE_OF_EMERGENCY),
                new ResetActingPlayerEvent()));
    }
}
