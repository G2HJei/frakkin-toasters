package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Source;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.ALERT_VIPER_PILOT;

// Vipers were launched from the "Reserves" ("Launch Reserves") and activated: Lee "Apollo" Adama may pilot one of them,
// if he can then. Queued only in games where someone has the ability.
@Source(ability = ALERT_VIPER_PILOT)
public record OfferAlertViperPilotEvent(List<Integer> viperIds) implements Event {

    public OfferAlertViperPilotEvent {
        viperIds = List.copyOf(viperIds);
    }
}
