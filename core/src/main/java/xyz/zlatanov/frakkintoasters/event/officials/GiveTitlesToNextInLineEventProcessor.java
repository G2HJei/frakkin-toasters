package xyz.zlatanov.frakkintoasters.event.officials;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class GiveTitlesToNextInLineEventProcessor extends EventProcessor<GiveTitlesToNextInLineEvent> {

    @Override
    public Followup process() {
        val character = player.character();
        val followups = new ArrayList<Event>();
        if (game.president() == character) {
            followups.add(new DetermineNextPresidentEvent(player.number()));
        }
        if (game.admiral() == character) {
            followups.add(new DetermineNextAdmiralEvent(player.number()));
        }
        if (game.cag() == character) {
            followups.add(new DetermineNextCagEvent(player.number()));
        }
        return all(followups.toArray(Event[]::new));
    }
}
