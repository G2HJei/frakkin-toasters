package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// A Cylon Leader draws his 2 Motive Cards, as the setup deals them. Test-only: with a StackDeckEvent before it, a test
// chooses the cards.
public record DrawMotiveCardsEvent(int playerNumber) implements PlayerEvent {
}
