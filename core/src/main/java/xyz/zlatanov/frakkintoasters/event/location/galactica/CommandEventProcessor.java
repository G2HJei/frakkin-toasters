package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.canActivateUnmanned;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;

public class CommandEventProcessor extends LocationActionProcessor<CommandEvent> {

    @Override
    public Followup process() {
        if (!canActivateUnmanned(game)) {
            // The decision would have no answer. An activation leaves a viper to activate, so this covers both.
            return NONE;
        }
        return all(activateUnmannedViper(), activateUnmannedViper()); // "up to 2", so the player may stop after each
    }

    private Followup activateUnmannedViper() {
        return one(new PlayerDecisionEvent<>(player.number(), ActivateViperEvent.class), new NoOpEvent(player.number()));
    }
}
