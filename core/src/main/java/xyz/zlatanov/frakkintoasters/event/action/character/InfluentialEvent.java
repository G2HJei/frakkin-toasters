package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

// Tory Foster's "Influential", once per game, President or not: she looks at the top 5 Quorum Cards and puts them
// back in the order she likes (ReorderTopQuorumCardsEvent).
public record InfluentialEvent(int playerNumber) implements CharacterActionEvent {
}
