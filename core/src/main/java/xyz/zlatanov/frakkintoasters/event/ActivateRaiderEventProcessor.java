package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import java.util.Comparator;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA_SPACE;

public class ActivateRaiderEventProcessor extends EventProcessor<ActivateRaiderEvent> {

    private static final List<Location> SPACE_AREAS_CLOCKWISE = GALACTICA_SPACE.locations();

    @Override
    public Followup process() {
        val raider = galacticaBoard.shipInSpace(event.raiderShipId(), Raider.class);
        val location = galacticaBoard.locate(raider);

        val humanFighters = galacticaBoard.humanFightersIn(location);
        if (!humanFighters.isEmpty()) {
            return attackViper(raider, humanFighters);
        }

        val civiliansHere = galacticaBoard.shipsIn(location, CivilianShip.class);
        if (!civiliansHere.isEmpty()) {
            return destroyCivilianShip(civiliansHere);
        }

        val allCivilians = galacticaBoard.shipsInSpace(CivilianShip.class);
        if (!allCivilians.isEmpty()) {
            moveTowardNearestCivilian(raider, location, allCivilians);
            return Followup.NONE;
        }

        return single(new AttackGalacticaEvent(raider.id()));
    }

    private Followup attackViper(Raider raider, List<HumanFighter> humanFighters) {
        val unmanned = humanFighters.stream().filter(s -> s.pilot() == null).toList();
        val targets = !unmanned.isEmpty() ? unmanned : humanFighters;
        return one(targets.stream()
                .sorted(Comparator.comparingInt(HumanFighter::id))
                .map(s -> new AttackViperEvent(raider.id(), s.id()))
                .toArray(Event[]::new));
    }

    private Followup destroyCivilianShip(List<CivilianShip> civilians) {
        return one(civilians.stream()
                .map(c -> new DestroyCivilianShipEvent(c.id()))
                .toArray(Event[]::new));
    }

    private void moveTowardNearestCivilian(Raider raider, Location raiderLocation,
                                           List<CivilianShip> civilians) {
        val galactica = galacticaBoard;
        val raiderIndex = SPACE_AREAS_CLOCKWISE.indexOf(raiderLocation);
        val size = SPACE_AREAS_CLOCKWISE.size();

        int bestDist = Integer.MAX_VALUE;
        boolean moveClockwise = true;

        for (val civilian : civilians) {
            val civIndex = SPACE_AREAS_CLOCKWISE.indexOf(galactica.locate(civilian));
            val clockwiseDist = (civIndex - raiderIndex + size) % size;
            val counterClockwiseDist = (raiderIndex - civIndex + size) % size;
            val minDist = Math.min(clockwiseDist, counterClockwiseDist);

            if (minDist < bestDist) {
                bestDist = minDist;
                moveClockwise = clockwiseDist <= counterClockwiseDist;
            } else if (minDist == bestDist && clockwiseDist <= counterClockwiseDist) {
                moveClockwise = true;
            }
        }

        val nextIndex = moveClockwise
                ? (raiderIndex + 1) % size
                : (raiderIndex - 1 + size) % size;
        game.moveTo(SPACE_AREAS_CLOCKWISE.get(nextIndex), raider);
    }
}
