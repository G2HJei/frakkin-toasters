package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnBottomEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnTopEvent;

import java.util.ArrayList;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class GasCloudPlaceEventProcessor extends EventProcessor<GasCloudPlaceEvent> {

    @Override
    protected boolean isValid() {
        return game.playerNumber(game.admiral()) == event.playerNumber();
    }

    @Override
    public Followup process() {
        if (event.cards().isEmpty()) {
            return Followup.NONE;
        }
        // One card at a time: which cards are left to choose from depends on the last choice. Equal cards collapse.
        val options = new ArrayList<Followup>();
        for (val card : event.cards().stream().distinct().toList()) {
            val rest = new ArrayList<>(event.cards());
            rest.remove(card);
            val next = single(new GasCloudPlaceEvent(event.playerNumber(), List.copyOf(rest)));
            options.add(all(single(new PlaceCrisisCardOnTopEvent(event.playerNumber(), card)), next));
            options.add(all(single(new PlaceCrisisCardOnBottomEvent(event.playerNumber(), card)), next));
        }
        return one(options.toArray(Followup[]::new));
    }
}
