package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.board.Location;

public interface LocationEvent extends Event {

    Location location();
}
