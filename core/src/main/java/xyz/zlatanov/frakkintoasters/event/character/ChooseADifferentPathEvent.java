package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

// Lee Adama's "Choose a Different Path" (once per game): when he has to choose on a Crisis Card, the result may instead
// be that the current player discards 5 Skill Cards.
public record ChooseADifferentPathEvent(int playerNumber, CrisisCard card) implements PlayerEvent {
}
