package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class MysteriousMessagePassEventProcessor extends EventProcessor<MysteriousMessagePassEvent> {

    // "may": the current player searches the deck, or leaves it.
    @Override
    public Followup process() {
        val current = game.turn().currentPlayerNumber();
        // The cards of the deck are his options, which only he sees.
        val options = new ArrayList<Event>(DiscardFromDestinyDeckEventProcessor.options(game, current));
        options.add(new NoOpEvent(current));
        return one(options.toArray(Event[]::new));
    }
}
