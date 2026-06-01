package xyz.zlatanov.frakkintoasters.event.officials;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.List;
import java.util.Optional;

public interface LineOfSuccessionSelector {

    List<Character> lineOfSuccession(Game game);

    default Character calcNextInLine(Game game) {
        val lineOfSuccession = lineOfSuccession(game);
        Character nextInLine = null;
        var nextRank = 99;
        for (val player : game.players()) {
            val character = player.character();
            val rank = lineOfSuccession.indexOf(character);
            if (rank > -1 && rank < nextRank) {
                nextRank = rank;
                nextInLine = character;
            }
        }
        return Optional.ofNullable(nextInLine)
                .orElseGet(this::noNextInLine);
    }

    default Character noNextInLine() {
        throw new FrakCallTheAdmiralException();
    }
}
