package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;

public record PlaySuperCrisisCardActionEvent(int playerNumber, SuperCrisisCard superCrisisCard) implements ActionEvent {

    //todo follow up with chosen card's specific event
}
