package xyz.zlatanov.frakkintoasters.event.action.title;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;

// The President Title Card's action: draw a Quorum Card into the President's hand.
public record PresidentTitleActionEvent(int playerNumber) implements ActionEvent {
}
