package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

public interface PlayerEvent extends Event {

    //todo maybe playerNumber is not needed since the game turn will handle which action is handled by which player (current, other via executive order etc.)

    int playerNumber();

    default PlayerView player(GameView game) {
        return game.player(playerNumber());
    }
}
