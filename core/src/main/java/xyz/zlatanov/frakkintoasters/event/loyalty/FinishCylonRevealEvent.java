package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Steps 2 to 7 of a Cylon's reveal (rule 6.1.1): the titles, the "Resurrection Ship", a Super Crisis Card (not after an
// execution, rule 7.1), the other Loyalty Cards and the Mutineer card to humans, and the end of his turn.
public record FinishCylonRevealEvent(int playerNumber, boolean drawsSuperCrisisCard) implements PlayerEvent {
}
