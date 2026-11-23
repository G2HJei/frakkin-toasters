package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.SKILLED_POLITICIAN;

// The Quorum Card Laura Roslin resolves after "Skilled Politician", and the others, which go to the bottom.
@Source(ability = SKILLED_POLITICIAN)
public record SkilledPoliticianChoiceEvent(int playerNumber, QuorumCard chosen, List<QuorumCard> others) implements PlayerEvent {
}
