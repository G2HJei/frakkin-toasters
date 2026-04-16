package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlaySuperCrisisCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.followWith;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public record CapricaActionEvent(int playerNumber) implements ActionEvent {

    @Override
    public List<Followup> apply(Game game) {
        //todo remove this event and implement directly CapricaOption1ActionEvent / option2
        return followWith(one(
                new PlayerDecisionEvent(playerNumber, PlaySuperCrisisCardEvent.class),
                new CapricaActionOption2Event(playerNumber)));
    }
}
