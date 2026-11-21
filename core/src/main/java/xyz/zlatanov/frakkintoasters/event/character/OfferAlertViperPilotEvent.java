package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.Event;

import java.util.List;

// Vipers were launched from the "Reserves" ("Launch Reserves") and activated: Lee "Apollo" Adama may pilot one of them,
// if he can then. Queued only in games where someone has the ability.
public record OfferAlertViperPilotEvent(List<Integer> viperIds) implements Event {

    public OfferAlertViperPilotEvent {
        viperIds = List.copyOf(viperIds);
    }
}
