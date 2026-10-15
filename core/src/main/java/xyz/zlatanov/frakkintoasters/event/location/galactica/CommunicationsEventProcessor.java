package xyz.zlatanov.frakkintoasters.event.location.galactica;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.MoveCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import java.util.Objects;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class CommunicationsEventProcessor extends EventProcessor<CommunicationsEvent> {
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
