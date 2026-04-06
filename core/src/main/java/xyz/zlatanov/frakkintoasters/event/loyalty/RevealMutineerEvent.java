package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.event.Event;

public record RevealMutineerEvent() implements Event {
    @Override
    public void apply(Game game) {
        //todo
    }
}
