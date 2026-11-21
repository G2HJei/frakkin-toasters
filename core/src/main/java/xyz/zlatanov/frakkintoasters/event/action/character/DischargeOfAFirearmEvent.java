package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

// Callandra "Cally" Tyrol's "Discharge of a Firearm", once per game: another character at her location is
// executed.
public record DischargeOfAFirearmEvent(int playerNumber, int targetPlayerNumber) implements CharacterActionEvent {
}
