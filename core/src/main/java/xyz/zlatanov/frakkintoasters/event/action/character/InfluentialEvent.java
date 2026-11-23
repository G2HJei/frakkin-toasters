package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.INFLUENTIAL;

// Tory Foster's "Influential", once per game, President or not: she looks at the top 5 Quorum Cards and puts them
// back in the order she likes (ReorderTopQuorumCardsEvent).
@Source(ability = INFLUENTIAL)
public record InfluentialEvent(int playerNumber) implements CharacterActionEvent {
}
