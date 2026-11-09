package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.DamageHumanFighterEvent;
import xyz.zlatanov.frakkintoasters.event.DestroyRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toSet;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA_SPACE;

public class MainBatteriesEventProcessor extends LocationActionProcessor<MainBatteriesEvent> {

    @Override
    protected boolean isValidAction() {
        return event.spaceLocation() != null && GALACTICA_SPACE.locations().contains(event.spaceLocation());
    }

    @Override
    public Followup process() {
        val roll = ops.rollDie();
        if (roll == 1) {
            return destroyCivilianShip();
        } else if (roll <= 3) {
            return damageViper();
        } else if (roll <= 6) {
            return destroyRaiders(2);
        } else {
            return destroyRaiders(4);
        }
    }

    // The options are the ships in the chosen space area, which a decision on the bare event couldn't keep.
    private Followup destroyCivilianShip() {
        return one(galacticaBoard.shipsIn(event.spaceLocation(), CivilianShip.class)
                .stream()
                .sorted(comparingInt(Ship::id))
                .map(civilianShip -> new DestroyCivilianShipEvent(civilianShip.id()))
                .toArray(Event[]::new));
    }

    private Followup damageViper() {
        return one(galacticaBoard.humanFightersIn(event.spaceLocation())
                .stream()
                .sorted(comparingInt(Ship::id))
                .map(humanFighter -> new DamageHumanFighterEvent(humanFighter.id()))
                .toArray(Event[]::new));
    }

    // Raiders are all alike, so nobody has to choose which ones are destroyed.
    private Followup destroyRaiders(int count) {
        val raiderIds = galacticaBoard.shipsIn(event.spaceLocation(), Raider.class)
                .stream()
                .map(Ship::id)
                .sorted()
                .limit(count)
                .collect(toSet());
        return raiderIds.isEmpty() ? Followup.NONE : single(new DestroyRaidersEvent(raiderIds));
    }
}
