package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.RAPTOR_PILOT;

// Karl "Helo" Agathon (alt.)'s "Raptor Pilot": where "Launch Scout" looks at the top card of a deck, he looks at the top
// 2 and puts each on the top or the bottom, in the order he likes.
@Source(ability = RAPTOR_PILOT)
public record RaptorPilotEvent(int playerNumber, DeckId deck) implements PlayerEvent {
}
