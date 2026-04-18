package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Game;

public record RevealMutineerEvent() implements Event {
    @Override
    public Followup apply(Game game) {
        //todo
        return Followup.NONE;
    }
}
