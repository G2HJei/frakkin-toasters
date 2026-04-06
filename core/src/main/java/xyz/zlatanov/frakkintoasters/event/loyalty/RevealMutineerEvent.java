package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.Game;

public record RevealMutineerEvent() implements Event {
    @Override
    public void apply(Game game) {
        //todo
    }
}
