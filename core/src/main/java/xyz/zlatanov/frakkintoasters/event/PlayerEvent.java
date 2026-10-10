package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;

public interface PlayerEvent extends Event {

    //todo maybe playerNumber is not needed since the game turn will handle which action is handled by which player (current, other via executive order etc.)

    int playerNumber();

    default Player player(Game game) {
        return (Player) game.player(playerNumber());
    }
}
