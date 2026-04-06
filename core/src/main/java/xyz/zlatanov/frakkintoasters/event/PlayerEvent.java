package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.Player;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;

public interface PlayerEvent extends Event {

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
