package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.event.action.ResetActingPlayerEvent;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.MOVE_OR_ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.STATE_OF_EMERGENCY;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;

public class StateOfEmergencyEventProcessor extends SkillCardEventProcessor<StateOfEmergencyEvent> {

    @Override
    public SkillCardType type() {
        return SkillCardType.STATE_OF_EMERGENCY;
    }

    // Cylon players take part too, because the card targets nobody (A2.3.8.6). A revealed Cylon's action is still only
    // his location's (rule 6.1.3.3).
    @Override
    protected Followup resolve() {
        val events = new ArrayList<Event>();
        events.add(lose(FOOD, 1));
        playersFrom(game, player.number())
                .forEach(each -> events.add(new GrantEvent(each.number(), MOVE_OR_ACTION, STATE_OF_EMERGENCY)));
        events.add(new ResetActingPlayerEvent());
        return all(events.toArray(Event[]::new));
    }
}
