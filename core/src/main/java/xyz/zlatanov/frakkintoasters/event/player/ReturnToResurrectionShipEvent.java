package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;

// Infiltration Card
// Action: an Infiltrating Cylon Leader ends his Infiltration and goes back to the "Resurrection Ship". If he was in the
// "Brig", he then discards down to 3 Skill Cards (rule 6.2.3.5).
public record ReturnToResurrectionShipEvent(int playerNumber) implements ActionEvent {
}
