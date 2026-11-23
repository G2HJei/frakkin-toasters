package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.Title;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MANIPULATIVE;

// Ellen Tigh's "Manipulative" (once per game): at the start of her turn she takes the Admiral or the President title,
// and gives it back to its previous holder at the end of her turn.
@Source(ability = MANIPULATIVE)
public record ManipulativeEvent(int playerNumber, Title title) implements PlayerEvent {
}
