package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.Title;

// Ellen Tigh's "Manipulative" (once per game): at the start of her turn she takes the Admiral or the President title,
// and gives it back to its previous holder at the end of her turn.
public record ManipulativeEvent(int playerNumber, Title title) implements PlayerEvent {
}
