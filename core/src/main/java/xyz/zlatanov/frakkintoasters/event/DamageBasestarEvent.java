package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

public record DamageBasestarEvent() implements Event {
    @Override
    public List<Followup> apply(Game game) {
        //todo
        return Event.super.apply(game);
    }
}
