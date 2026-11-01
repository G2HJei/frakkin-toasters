package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;

// President's Office (Colonial One location, Daybreak)
// Action: Draw 2 politics Skill Cards.
public record PresidentsOfficeEvent(int playerNumber) implements ActionEvent {
}
