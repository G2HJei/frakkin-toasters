package xyz.zlatanov.frakkintoasters.action;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;

@Builder
@Getter
@Accessors(fluent = true)
public class SelectCharacterGameAction implements GameAction {
    private int       playerNumber;
    private Character selectedCharacter;

    @Override
    public void apply(Game game) {
        game.player(playerNumber).selectCharacter(selectedCharacter);
        game.moveTo(COMMAND, selectedCharacter);
    }
}
