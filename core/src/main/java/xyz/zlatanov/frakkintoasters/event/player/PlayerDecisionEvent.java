package xyz.zlatanov.frakkintoasters.event.player;

import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

@Accessors(fluent = true)
public record PlayerDecisionEvent(int playerNumber, Class<? extends Event> action) implements PlayerEvent {
    @Override
    public void apply(Game game) {

    }
}
