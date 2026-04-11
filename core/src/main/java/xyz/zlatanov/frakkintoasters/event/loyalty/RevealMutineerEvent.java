package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

public record RevealMutineerEvent() implements Event {
    @Override
    public List<Followup> apply(Game game) {
        //todo
        return List.of();
    }
}
