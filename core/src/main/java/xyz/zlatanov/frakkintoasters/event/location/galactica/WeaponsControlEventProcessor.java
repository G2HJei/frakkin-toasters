package xyz.zlatanov.frakkintoasters.event.location.galactica;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.AttackBasestarEvent;
import xyz.zlatanov.frakkintoasters.event.AttackHeavyRaiderEvent;
import xyz.zlatanov.frakkintoasters.event.AttackRaiderEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShip;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import static xyz.zlatanov.frakkintoasters.event.AttackBasestarEvent.Attacker.GALACTICA;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class WeaponsControlEventProcessor extends EventProcessor<WeaponsControlEvent> {
    @Override
    public Followup process() {
        val shipId = event.cylonShipId();
        val ship = galacticaBoard.shipInSpace(shipId, CylonShip.class);
        if (ship instanceof Raider) {
            return single(new AttackRaiderEvent(shipId));
        } else if (ship instanceof HeavyRaider) {
            return single(new AttackHeavyRaiderEvent(shipId));
        } else {
            return single(new AttackBasestarEvent(GALACTICA, shipId));
        }
    }
}
