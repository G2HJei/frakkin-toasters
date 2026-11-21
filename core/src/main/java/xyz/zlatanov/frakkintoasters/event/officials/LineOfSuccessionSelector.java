package xyz.zlatanov.frakkintoasters.event.officials;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public interface LineOfSuccessionSelector {

    List<Character> lineOfSuccession();

    default Character calcNextInLine(GameView game, Integer excludedPlayerNumber) {
        return Optional.ofNullable(nextInLine(game, excludedPlayerNumber, character -> true))
                .orElseGet(this::noNextInLine);
    }

    /**
     * @return the character highest in line whose player can hold a title and who passes the filter, {@code null} when
     * there is none
     */
    default Character nextInLine(GameView game, Integer excludedPlayerNumber, Predicate<Character> eligible) {
        val lineOfSuccession = lineOfSuccession();
        Character nextInLine = null;
        var nextRank = 99;
        for (val player : game.players()) {
            val character = player.character();
            // Revealed Cylons hold no titles (rule 6.1.1).
            if (Objects.equals(player.number(), excludedPlayerNumber) || character == null || !player.isHuman()) {
                continue;
            }
            val rank = lineOfSuccession.indexOf(character);
            if (rank > -1 && rank < nextRank && eligible.test(character)) {
                nextRank = rank;
                nextInLine = character;
            }
        }
        return nextInLine;
    }

    default Character noNextInLine() {
        throw new FrakCallTheAdmiralException();
    }
}
