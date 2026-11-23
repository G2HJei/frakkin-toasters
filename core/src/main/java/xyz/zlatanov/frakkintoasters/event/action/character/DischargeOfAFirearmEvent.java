package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.DISCHARGE_OF_A_FIREARM;

// Callandra "Cally" Tyrol's "Discharge of a Firearm", once per game: another character at her location is
// executed.
@Source(ability = DISCHARGE_OF_A_FIREARM)
public record DischargeOfAFirearmEvent(int playerNumber, int targetPlayerNumber) implements CharacterActionEvent {
}
