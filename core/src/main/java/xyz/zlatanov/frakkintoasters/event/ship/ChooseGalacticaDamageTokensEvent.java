package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;

import java.util.List;

// The player drew Galactica damage tokens and resolves some of them, in order, e.g. "Can damage Galactica" (a "You Are
// a Cylon" card). The others go back to the pile.
public record ChooseGalacticaDamageTokensEvent(int playerNumber, List<GalacticaDamage> resolved,
                                               List<GalacticaDamage> returned) implements PlayerEvent {
}
