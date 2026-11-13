package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;

// The player drew 2 Galactica damage tokens and chooses the one to resolve, e.g. at the "Basestar Bridge". The other
// goes back to the pile.
public record ChooseGalacticaDamageEvent(int playerNumber, GalacticaDamage resolved,
                                         GalacticaDamage returned) implements PlayerEvent {
}
