package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// "Can damage Galactica": draw up to 5 Galactica damage tokens, resolve 2 of them, the others go back.
public record CylonPowerDamageGalacticaEvent(int playerNumber) implements PlayerEvent {
}
