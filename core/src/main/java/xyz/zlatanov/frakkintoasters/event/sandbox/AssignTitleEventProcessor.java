package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class AssignTitleEventProcessor extends EventProcessor<AssignTitleEvent> {

    @Override
    protected boolean isValid() {
        return event.title() != null && event.character() != null
                && game.players().stream().anyMatch(player -> player.character() == event.character());
    }

    @Override
    public Followup process() {
        switch (event.title()) {
            case PRESIDENT -> ops.titles().assignPresident(event.character());
            case ADMIRAL -> ops.titles().assignAdmiral(event.character());
            case CAG -> ops.titles().assignCag(event.character());
        }
        return Followup.NONE;
    }
}
