package xyz.zlatanov.frakkintoasters.event.location;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.MoveCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import java.util.Objects;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class CommunicationsEventProcessor extends EventProcessor<CommunicationsEvent> {
    @Override
    public Followup process() {
        return all(
                Stream.of(event.civilianShipId1(),
                                event.civilianShipId2())
                        .filter(Objects::nonNull)
                        .peek(civId -> player.revealCivilianShip(civId, 2))
                        .map(this::moveFollowups)
                        .map(Followup::one)
                        .toArray(Followup[]::new));
    }

    private Event[] moveFollowups(int civilianShipId) {
        val civShip = galacticaBoard.shipInSpace(civilianShipId, CivilianShip.class);
        val location = galacticaBoard.locate(civShip);
        val adjacentLocations = location.adjacentLocations();
        return new Event[]{
                new NoOpEvent(player.number()),
                new MoveCivilianShipEvent(civilianShipId, adjacentLocations.getFirst()),
                new MoveCivilianShipEvent(civilianShipId, adjacentLocations.getLast())};
    }
}
