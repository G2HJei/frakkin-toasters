package xyz.zlatanov.frakkintoasters.event.location.galactica;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.MoveCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class CommunicationsEventProcessor extends LocationActionProcessor<CommunicationsEvent> {

    // 2 different civilian ships in space, or all of them when fewer are left.
    @Override
    protected boolean isValidAction() {
        val inSpace = galacticaBoard.shipsInSpace(CivilianShip.class).stream().map(Ship::id).collect(toSet());
        val chosen = Stream.of(event.civilianShipId1(), event.civilianShipId2()).filter(Objects::nonNull).toList();
        return inSpace.containsAll(chosen)
                && new HashSet<>(chosen).size() == chosen.size()
                && chosen.size() == Math.min(2, inSpace.size());
    }

    @Override
    public Followup process() {
        return all(
                Stream.of(event.civilianShipId1(),
                                event.civilianShipId2())
                        .filter(Objects::nonNull)
                        .peek(this::revealCivilianShip)
                        .map(this::moveFollowups)
                        .toArray(Followup[]::new));
    }

    private void revealCivilianShip(Integer civId) {
        ops.player(player.number()).lookAtShip(civId);
    }

    private Followup moveFollowups(int civilianShipId) {
        val civShip = galacticaBoard.shipInSpace(civilianShipId, CivilianShip.class);
        val location = galacticaBoard.locate(civShip);
        val adjacentLocations = location.adjacentLocations();
        return one(
                new NoOpEvent(player.number()),
                new MoveCivilianShipEvent(civilianShipId, adjacentLocations.getFirst()),
                new MoveCivilianShipEvent(civilianShipId, adjacentLocations.getLast()));
    }
}
