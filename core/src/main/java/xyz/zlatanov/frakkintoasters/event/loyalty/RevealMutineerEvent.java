package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.Event;

// You Are The Mutineer (loyalty card, Daybreak)
// IMMEDIATELY REVEAL THIS CARD
// If you receive this card facedown, immediately reveal it and draw another Loyalty Card. Any time you receive this
// card, lose your titles and draw a Mutiny Card. If you reveal yourself as a Cylon, give this card faceup to a human
// player of your choice.
// Do not move to the "Brig" when you gain a second Mutiny Card. If you gain a third Mutiny Card, you must move to the
// "Brig."
// WHEN YOU RESOLVE A "PREPARE FOR JUMP" ICON, YOU MUST DRAW A MUTINY CARD.
public record RevealMutineerEvent() implements Event {
//todo
}
