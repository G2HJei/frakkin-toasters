package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.RELIGIOUS_VISIONS;

// Laura Roslin's "Religious Visions": she draws 2 Crisis Cards, resolves the one she picks and puts the other at the
// bottom of the deck.
@Source(ability = RELIGIOUS_VISIONS)
public record ReligiousVisionsEvent(int playerNumber, CrisisCard crisisCardToPlay, CrisisCard crisisCardToPutAtBottom)
        implements PlayerEvent {
}
