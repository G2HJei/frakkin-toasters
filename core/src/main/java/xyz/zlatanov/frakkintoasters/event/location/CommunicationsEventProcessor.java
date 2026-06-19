package xyz.zlatanov.frakkintoasters.event.location;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.MoveCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import java.util.Map;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.DISTANCE_LOOKUP_TABLE;

public class CommunicationsEventProcessor extends EventProcessor<CommunicationsEvent> {
    @Override
    public Followup process() {
        val id1 = event.civilianShipId1();
        val id2 = event.civilianShipId2();

        player.revealCivilianShip(id1, 2);
        player.revealCivilianShip(id2, 2);
        return all(
                one(moveFollowups(id1)),
                one(moveFollowups(id2)));
    }

    private Event[] moveFollowups(int civilianShipId) {
        val civShip = galacticaBoard.shipInSpace(civilianShipId, CivilianShip.class);
        val location = galacticaBoard.locate(civShip);
        val adjacentLocations = DISTANCE_LOOKUP_TABLE.get(location)
                .entrySet()
                .stream()
                .filter(es -> es.getValue() == 1)
                .map(Map.Entry::getKey)
                .toArray(Location[]::new);
        return new Event[]{
                new NoOpEvent(player.number()),
                new MoveCivilianShipEvent(civilianShipId, adjacentLocations[0]),
                new MoveCivilianShipEvent(civilianShipId, adjacentLocations[1])};
    }
}
