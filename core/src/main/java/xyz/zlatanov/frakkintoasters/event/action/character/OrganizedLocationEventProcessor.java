package xyz.zlatanov.frakkintoasters.event.action.character;

import java.util.ArrayList;
import java.util.List;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class OrganizedLocationEventProcessor extends EventProcessor<OrganizedLocationEvent> {

    @Override
    protected boolean isValid() {
        return event.location() != null && event.used() != null && !event.used().contains(event.location())
                && event.used().size() < OrganizedEventProcessor.LOCATIONS
                && RemoteActions.locations(game, player, false).contains(event.location());
    }

    @Override
    public Followup process() {
        val used = new ArrayList<>(event.used());
        used.add(event.location());
        return all(RemoteActions.actAt(ops, event.playerNumber(), event.location()),
                single(new OrganizedNextEvent(event.playerNumber(), List.copyOf(used))));
    }
}
