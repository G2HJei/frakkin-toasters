package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.PilotableShip;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import java.util.Comparator;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.LOCATION_AREAS;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.CIVILIAN;

public record ActivateRaiderEvent(int raiderShipId) implements Event {

    private static final List<Location> SPACE_AREAS_CLOCKWISE = LOCATION_AREAS.get("Galactica space");

    @Override
    public Followup apply(Game game) {
        val galactica = game.boards().galactica();
        val raider = galactica.shipsInSpace(Raider.class).stream()
                .filter(r -> r.id() == raiderShipId)
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new);
        val location = galactica.locate(raider);

        val pilotables = galactica.shipsIn(location).stream()
                .filter(PilotableShip.class::isInstance)
                .map(PilotableShip.class::cast)
                .toList();
        if (!pilotables.isEmpty()) {
            return attackViper(raider, pilotables);
        }

        val civiliansHere = galactica.shipsIn(location, CIVILIAN);
        if (!civiliansHere.isEmpty()) {
            return destroyCivilianShip(civiliansHere);
        }

        val allCivilians = galactica.shipsInSpace(CivilianShip.class);
        if (!allCivilians.isEmpty()) {
            moveTowardNearestCivilian(game, raider, location, allCivilians);
            return Followup.NONE;
        }

        return single(new AttackGalacticaEvent(raider.id()));
    }

    private Followup attackViper(Raider raider, List<PilotableShip> pilotables) {
        val unmanned = pilotables.stream().filter(s -> s.pilot() == null).toList();
        val targets = !unmanned.isEmpty() ? unmanned : pilotables;
        if (targets.size() == 1) {
            return single(new AttackViperEvent(raider.id(), targets.getFirst().id()));
        }
        return one(targets.stream()
                .sorted(Comparator.comparingInt(PilotableShip::id))
                .map(s -> new AttackViperEvent(raider.id(), s.id()))
                .toArray(Event[]::new));
    }

    private Followup destroyCivilianShip(List<Ship> civilians) {
        if (civilians.size() == 1) {
            return single(new DestroyCivilianShipEvent(civilians.getFirst().id()));
        }
        return one(civilians.stream()
                .map(c -> new DestroyCivilianShipEvent(c.id()))
                .toArray(Event[]::new));
    }

    private void moveTowardNearestCivilian(Game game, Raider raider, Location raiderLocation,
                                           List<CivilianShip> civilians) {
        val galactica = game.boards().galactica();
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
