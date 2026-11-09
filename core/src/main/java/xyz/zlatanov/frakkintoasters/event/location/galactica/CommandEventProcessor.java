package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class CommandEventProcessor extends LocationActionProcessor<CommandEvent> {

    // "Up to 2", so the player may stop after each one, e.g. when no unmanned viper is left.
    @Override
    public Followup process() {
        return all(activateUnmannedViper(), activateUnmannedViper());
    }

    private Followup activateUnmannedViper() {
        return one(new PlayerDecisionEvent<>(player.number(), ActivateViperEvent.class), new NoOpEvent(player.number()));
    }
}
