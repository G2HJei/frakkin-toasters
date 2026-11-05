package xyz.zlatanov.frakkintoasters.event.location.cylon;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;

// Human Fleet (Cylon location, Daybreak)
// Action: Look at the top card of the Crisis or Destination deck and place it on the top or bottom of that deck. Then,
// draw 2 Skill Cards.
// or
// Action: Infiltrate Galactica.
public record HumanFleetLookAtTopCrisisCardEvent(int playerNumber) implements ActionEvent {
}
