package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;

// D'Anna Biers' "Heretic": the Super Crisis Card she discards for leaving the "Resurrection Ship". The move queues it,
// one option per card she holds.
public record HereticDiscardEvent(int playerNumber, SuperCrisisCard card) implements PlayerEvent {
}
