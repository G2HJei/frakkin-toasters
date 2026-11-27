package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA_SPACE;

public class ActivateRaiderAreasEventProcessor extends EventProcessor<ActivateRaiderAreasEvent> {

    @Override
    protected boolean isValid() {
        return event.raiderShipIds() != null && event.raiderShipIds().stream().allMatch(Objects::nonNull);
    }

    // "If multiple space areas on the main game board contain ships that need to be activated, the ships are activated
    // one entire space area at a time in the order of the current player's choice." (rule 11.5) With one area there is
    // nothing to choose.
    @Override
    public Followup process() {
        val areas = areasOf(game, event.raiderShipIds());
        if (areas.isEmpty()) {
            return NONE;
        }
        return one(areas.stream()
                .map(area -> new ActivateRaiderAreaEvent(area, event.raiderShipIds()))
                .toArray(Event[]::new));
    }

    static Set<Location> areasOf(GameView game, List<Integer> raiderShipIds) {
        val galactica = game.boards().galactica();
        val inSpace = galactica.shipsInSpace(Raider.class).stream().map(Raider::id).toList();
        val areas = new LinkedHashSet<Location>();
        GALACTICA_SPACE.locations().forEach(area -> raiderShipIds.stream()
                .filter(inSpace::contains)
                .filter(id -> galactica.locate(galactica.shipInSpace(id, Raider.class)) == area)
                .findAny()
                .ifPresent(ignored -> areas.add(area)));
        return areas;
    }
}
