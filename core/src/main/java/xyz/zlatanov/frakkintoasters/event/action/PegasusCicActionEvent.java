package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;

public record PegasusCicActionEvent() implements Event {

    @Override
    public void apply(Game game) {
        game.damage(Location.PEGASUS_CIC);
    }
}
