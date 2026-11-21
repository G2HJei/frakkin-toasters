package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// "Can make players draw Treachery cards": each human discards a random Skill Card and draws a Treachery card, then the
// Cylon draws 2 Treachery cards. Not a "may".
public record CylonPowerDrawTreacheryEvent(int playerNumber) implements PlayerEvent {
}
