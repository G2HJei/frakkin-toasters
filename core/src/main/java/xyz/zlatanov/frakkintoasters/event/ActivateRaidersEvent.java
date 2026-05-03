package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.CIVILIAN;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

public record ActivateRaidersEvent() implements Event {

    private static final List<Location> SPACE_AREAS_CLOCKWISE = List.of(
            GALACTICA_SPACE_12_OCLOCK, GALACTICA_SPACE_2_OCLOCK,
            GALACTICA_SPACE_4_OCLOCK, GALACTICA_SPACE_6_OCLOCK,
            GALACTICA_SPACE_8_OCLOCK, GALACTICA_SPACE_10_OCLOCK);

    @Override
    public Followup apply(Game game) {
        val galactica = game.boards().galactica();
        val raiders = galactica.shipsInSpace(Raider.class);
        val basestars = galactica.shipsInSpace(Basestar.class);

        // Rule 6: No raiders or basestars on main board
        if (raiders.isEmpty() && basestars.isEmpty()) {
            return all(new PlaceShipOnCylonFleetBoardEvent(RAIDER), new AdvancePursuitTrackEvent());
        }

        // Rule 5: No raiders on board but basestars present
        if (raiders.isEmpty()) {
            launchRaiders(game, basestars);
            return Followup.NONE;
        }

        // Process each raider in clockwise order
        val sortedRaiders = raiders.stream()
                .sorted(Comparator.comparingInt(a -> SPACE_AREAS_CLOCKWISE.indexOf(galactica.locate(a))))
                .toList();
        val followups = new ArrayList<Followup>();
        for (val raider : sortedRaiders) {
            val location = galactica.locate(raider);
            val result = activateRaider(game, raider, location);
            if (result != null) {
                followups.add(result);
            }
        }

        if (followups.isEmpty()) {
            return Followup.NONE;
        }
        if (followups.size() == 1) {
            return followups.getFirst();
        }
        return new Followup.AllOf(followups);
    }

    private Followup activateRaider(Game game, Raider raider, Location location) {
        val galactica = game.boards().galactica();

        // Rule 1: Attack a Viper
        val vipers = galactica.shipsIn(location, ShipType.VIPER, ShipType.VIPER_MARK_VII).stream()
                .filter(s -> s instanceof PilotableShip)
                .map(s -> (PilotableShip) s)
                .toList();
        if (!vipers.isEmpty()) {
            return attackViper(vipers);
        }

        // Rule 2: Destroy Civilian Ship
        val civilians = galactica.shipsIn(location, CIVILIAN);
        if (!civilians.isEmpty()) {
            return destroyCivilianShip(civilians);
        }

        // Rule 3: Move toward nearest civilian ship
        val allCivilians = galactica.shipsInSpace(CivilianShip.class);
        if (!allCivilians.isEmpty()) {
            moveTowardNearestCivilian(game, raider, location, allCivilians);
            return null;
        }

        // Rule 4: Attack Galactica
        return single(new DamageGalacticaEvent());
    }

    private Followup attackViper(List<PilotableShip> vipers) {
        val unmanned = vipers.stream().filter(v -> v.pilot() == null).toList();
        if (!unmanned.isEmpty()) {
            return single(new DamageVipersEvent(Set.of(unmanned.getFirst().id())));
        }
        // all are piloted
        if (vipers.size() == 1) {
            return single(new DamageVipersEvent(Set.of(vipers.getFirst().id())));
        }
        // multiple piloted vipers - player chooses
        return new Followup.OneOf(vipers.stream()
                .sorted(Comparator.comparingInt(PilotableShip::id))
                .<Followup>map(v -> single(new DamageVipersEvent(Set.of(v.id()))))
                .toList());
    }

    private Followup destroyCivilianShip(List<Ship> civilians) {
        if (civilians.size() == 1) {
            return single(new DestroyCivilianShipEvent(civilians.getFirst().id()));
        }
        // multiple civilians - player chooses
        return new Followup.OneOf(civilians.stream()
                .sorted(Comparator.comparingInt(Ship::id))
                .<Followup>map(c -> single(new DestroyCivilianShipEvent(c.id())))
                .toList());
    }

    private void moveTowardNearestCivilian(Game game, Raider raider, Location raiderLocation,
                                           List<CivilianShip> civilians) {
        val galactica = game.boards().galactica();
        val raiderIndex = SPACE_AREAS_CLOCKWISE.indexOf(raiderLocation);
        val size = SPACE_AREAS_CLOCKWISE.size();

        int bestDist = Integer.MAX_VALUE;
        boolean moveClockwise = true;

        for (val civilian : civilians) {
            val civLocation = galactica.locate(civilian);
            val civIndex = SPACE_AREAS_CLOCKWISE.indexOf(civLocation);
            val clockwiseDist = (civIndex - raiderIndex + size) % size;
            val counterClockwiseDist = (raiderIndex - civIndex + size) % size;
            val minDist = Math.min(clockwiseDist, counterClockwiseDist);

            if (minDist < bestDist) {
                bestDist = minDist;
                moveClockwise = clockwiseDist <= counterClockwiseDist;
            } else if (minDist == bestDist && !moveClockwise && clockwiseDist <= counterClockwiseDist) {
                moveClockwise = true;
            }
        }

        Location nextStep;
        if (moveClockwise) {
            nextStep = SPACE_AREAS_CLOCKWISE.get((raiderIndex + 1) % size);
        } else {
            nextStep = SPACE_AREAS_CLOCKWISE.get((raiderIndex - 1 + size) % size);
        }
        game.moveTo(nextStep, raider);
    }

    private void launchRaiders(Game game, List<Basestar> basestars) {
        val galactica = game.boards().galactica();
        for (val basestar : basestars) {
            val basestarLocation = galactica.locate(basestar);
            for (int i = 0; i < 2; i++) {
                if (game.cylonShips().raiders().isEmpty()) {
                    break;
                }
                galactica.place(basestarLocation, game.cylonShips().raider());
            }
        }
    }
}
