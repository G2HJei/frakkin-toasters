package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// You Are The Mutineer (loyalty card, Daybreak)
// IMMEDIATELY REVEAL THIS CARD
// If you receive this card facedown, immediately reveal it and draw another Loyalty Card. Any time you receive this
// card, lose your titles and draw a Mutiny Card. If you reveal yourself as a Cylon, give this card faceup to a human
// player of your choice.
// Do not move to the "Brig" when you gain a second Mutiny Card. If you gain a third Mutiny Card, you must move to the
// "Brig."
// WHEN YOU RESOLVE A "PREPARE FOR JUMP" ICON, YOU MUST DRAW A MUTINY CARD.
//
// The card stays among the player's Loyalty Cards, and everyone knows it is there: the describers show it faceup.
//
// @param drawsLoyaltyCard the player was dealt the card facedown, so he draws another Loyalty Card. Not when it is the
//                         additional card of the "no Mutineer" case (rule 5.5.2.3)
public record RevealMutineerEvent(int playerNumber, boolean drawsLoyaltyCard) implements PlayerEvent {
}
