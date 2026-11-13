package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class AdministrationEventProcessor extends LocationActionProcessor<AdministrationEvent> {

    // The President's Mutiny Cards count after the draw, which may be the player's own as President.
    @Override
    public Followup process() {
        return all(new DrawMutinyCardEvent(player.number()), new ReplaceMutinousPresidentEvent(player.number()));
    }
}
