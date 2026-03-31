package xyz.zlatanov.frakkintoasters;

import lombok.val;
import xyz.zlatanov.frakkintoasters.action.Action;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.List;

public interface SelectNextOfficialAction extends Action {

    List<Character> lineOfSuccession();

    @Override
    default void apply(Game game) {
        val president = calcNextInLine(game, lineOfSuccession());
        game.president(president);
    }

    private Character calcNextInLine(Game game, List<Character> lineOfSuccession) {
        Character nextInLine = null;
        var nextRank = 99;
        for (val player : game.players().values()) {
            val character = player.character();
            val rank = lineOfSuccession.indexOf(character);
            if (rank > -1 && rank < nextRank) {
                nextRank = rank;
                nextInLine = character;
            }
        }
        if (nextInLine == null) {
            throw new FrakCallTheAdmiralException();
        }
        return nextInLine;
    }
}
