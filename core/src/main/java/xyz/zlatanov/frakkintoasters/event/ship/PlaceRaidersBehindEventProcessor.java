package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.ship.PlaceShipsEventProcessor.BEHIND;

public class PlaceRaidersBehindEventProcessor extends EventProcessor<PlaceRaidersBehindEvent> {

    @Override
    protected boolean isValid() {
        return event.count() >= 0;
    }

    @Override
    public Followup process() {
        // The ops take a raider from the Cylon Fleet board when the supply has none left (rule 11.4.2).
        for (int i = 0; i < event.count(); i++) {
            ops.galactica().placeRaider(BEHIND);
        }
        return Followup.NONE;
    }
}
