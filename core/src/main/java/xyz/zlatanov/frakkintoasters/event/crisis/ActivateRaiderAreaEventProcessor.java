package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class ActivateRaiderAreaEventProcessor extends EventProcessor<ActivateRaiderAreaEvent> {

    @Override
    protected boolean isValid() {
        return event.area() != null && event.raiderShipIds() != null
                && event.raiderShipIds().stream().allMatch(Objects::nonNull)
                && ActivateRaiderAreasEventProcessor.areasOf(game, event.raiderShipIds()).contains(event.area());
    }

    // Each raider activates once: those that move on into an area still to come aren't activated there again.
    @Override
    public Followup process() {
        val inSpace = galacticaBoard.shipsInSpace(Raider.class).stream().map(Raider::id).toList();
        val here = new ArrayList<Integer>();
        val rest = new ArrayList<Integer>();
        for (val id : event.raiderShipIds()) {
            if (inSpace.contains(id) && galacticaBoard.locate(galacticaBoard.shipInSpace(id, Raider.class)) == event.area()) {
                here.add(id);
            } else if (inSpace.contains(id)) {
                rest.add(id);
            }
        }
        val next = rest.isEmpty() ? Stream.<Event>empty() : Stream.<Event>of(new ActivateRaiderAreasEvent(rest));
        return all(Stream.concat(here.stream().map(ActivateRaiderEvent::new), next).toArray(Event[]::new));
    }
}
