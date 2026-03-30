package xyz.zlatanov.frakkintoasters.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.val;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.Arrays;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.Character.KARL_HELO_AGATHON;

@SuperBuilder
@Getter
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SelectCharacterAction extends PlayerAction {
    private final Character selectedCharacter;

    @Override
    public List<Action> apply(Game game) {
        game.player(playerNumber).selectCharacter(selectedCharacter);
        val setup = selectedCharacter.setup();
        if (setup.length == 1) {
            return moveToSetup(game);
        } else if (setup.length > 1) {
            return multipleSetupOptionsFollowup();
        } else {
            return specialSetupFollowup();
        }
    }

    private List<Action> moveToSetup(Game game) {
        game.moveTo(selectedCharacter.setup()[0], selectedCharacter);
        return List.of();
    }

    private List<Action> multipleSetupOptionsFollowup() {
        return Arrays.stream(selectedCharacter.setup())
                .map(loc -> (Action) MoveAction.builder()
                        .playerNumber(playerNumber)
                        .location(loc)
                        .build())
                .toList();
    }

    private List<Action> specialSetupFollowup() {
        if (selectedCharacter == KARL_HELO_AGATHON) {
            return List.of();
        }
        throw new FrakCallTheAdmiralException();
    }
}
