package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;

public interface PlayerEvent extends Event {

    //todo maybe playerNumber is not needed since the game turn will handle which action is handled by which player (current, other via executive order etc.)

    int playerNumber();

    default Player player(Game game) {
        return game.player(playerNumber());
    }

    default Character playerCharacter(Game game) {
        return game.player(playerNumber()).character();
    }

    default Location currentLocation(Game game) {
        return game.locate(playerCharacter(game));
    }
}
