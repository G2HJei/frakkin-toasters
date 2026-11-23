package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.INFLUENTIAL;

// The new order of the top Quorum Cards, the first on top. It has to hold the cards that are there.
@Source(ability = INFLUENTIAL)
public record ReorderTopQuorumCardsEvent(int playerNumber, List<QuorumCard> order) implements PlayerEvent {
}
