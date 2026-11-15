package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.DamageHumanFighterEvent;
import xyz.zlatanov.frakkintoasters.event.DestroyRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import static java.util.Comparator.comparingInt;
import static xyz.zlatanov.frakkintoasters.event.DestroyRaidersEventProcessor.destroyRaidersIn;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA_SPACE;

public class MainBatteriesResultEventProcessor extends EventProcessor<MainBatteriesResultEvent> {

    @Override
    protected boolean isValid() {
        return isSpaceArea(event.spaceLocation());
    }

    @Override
    public Followup process() {
        val roll = game.turn().dieRoll().result();
        if (roll == 1) {
            return destroyCivilianShip();
        } else if (roll <= 3) {
            return damageViper();
        } else if (roll <= 6) {
            return destroyRaidersIn(game, event.spaceLocation(), 2);
        } else {
            return destroyRaidersIn(game, event.spaceLocation(), 4);
        }
    }

    public static boolean isSpaceArea(Location location) {
        return location != null && GALACTICA_SPACE.locations().contains(location);
    }

    private Followup destroyCivilianShip() {
        // The options are the ships in the chosen space area, which a decision on the bare event couldn't keep.
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
}
