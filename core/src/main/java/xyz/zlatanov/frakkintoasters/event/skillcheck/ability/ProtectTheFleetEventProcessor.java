package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.AttackBasestarEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.MoveShipEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;

import static xyz.zlatanov.frakkintoasters.event.AttackBasestarEvent.Attacker.VIPER;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class ProtectTheFleetEventProcessor extends EventProcessor<ProtectTheFleetEvent> {

    @Override
    public Followup process() {
        if (event.launchShipType() != null) {
            return one(new LaunchViperEvent(event.launchShipType(), event.launchLocation()));
        }
        if (event.humanFighterId() != null && event.moveLocation() != null) {
            return one(new MoveShipEvent(event.humanFighterId(), event.moveLocation()));
        }
        if (event.humanFighterId() != null && event.attackCylonShipId() != null) {
            return one(new AttackBasestarEvent(VIPER, event.attackCylonShipId()));
        }
        if (event.escortCivilianShipId() != null) {
            ops.ship(event.escortCivilianShipId()).escort();
        }
        return NONE;
    }
}
