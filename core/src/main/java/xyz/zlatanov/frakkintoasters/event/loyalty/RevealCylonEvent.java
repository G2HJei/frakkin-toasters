package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;

// A hidden Cylon reveals a "You Are a Cylon" card as his action (rule 6.1), uses its power unless he is in the "Brig",
// and becomes a revealed Cylon (rule 6.1.1).
public record RevealCylonEvent(int playerNumber, LoyaltyCard card) implements ActionEvent {
}
