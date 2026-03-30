package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.Player;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_6_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.ASSAULT_RAPTOR;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.VIPER;

public record SelectCharacterAction(int playerNumber, Character selectedCharacter) implements Action {

    @Override
    public boolean isValid(Game game) {
        return !characterAlreadySelected(game)
                && !altAlreadySelected(game);
    }

    @Override
    public void apply(Game game) {
        game.player(playerNumber).selectCharacter(selectedCharacter);
    }

    @Override
    public List<Action> followup(Game game) {
        val setup = selectedCharacter.setup();
        if (setup.length == 1) {
            return moveToSetup(game);
        } else if (setup.length > 1) {
            return multipleSetupOptionsFollowup();
        } else {
            return specialSetupFollowup();
        }
    }

    private boolean characterAlreadySelected(Game game) {
        return game.players().values().stream()
                .map(Player::character)
                .anyMatch(c -> c == selectedCharacter);
    }

    private boolean altAlreadySelected(Game game) {
        val currentSelections = game.players().values().stream()
                .map(Player::character)
                .toList();
        val alternateAlreadySelected = Stream.of(
                        new Character[]{GAIUS_BALTAR, GAIUS_BALTAR_ALT},
                        new Character[]{KARL_HELO_AGATHON, KARL_HELO_AGATHON_ALT},
                        new Character[]{TOM_ZAREK, TOM_ZAREK_ALT},
                        new Character[]{SHARON_BOOMER_VALERII, SHARON_ATHENA_AGATHON},
                        new Character[]{LEE_APOLLO_ADAMA, LEE_ADAMA})
                .anyMatch(pair ->
                        (currentSelections.contains(pair[0]) && selectedCharacter == pair[1]) ||
                                (currentSelections.contains(pair[1]) && selectedCharacter == pair[0]));
    }

    private List<Action> moveToSetup(Game game) {
        game.moveTo(selectedCharacter.setup()[0], selectedCharacter);
        return List.of();
    }

    private List<Action> multipleSetupOptionsFollowup() {
        return Arrays.stream(selectedCharacter.setup())
                .map(loc -> (Action) new MoveAction(playerNumber, loc))
                .toList();
    }

    private List<Action> specialSetupFollowup() {
        return switch (selectedCharacter) {
            case KARL_HELO_AGATHON -> List.of();
            case LEE_APOLLO_ADAMA -> List.of(
                    new LaunchViperAction(playerNumber, VIPER, GALACTICA_SPACE_4_OCLOCK),
                    new LaunchViperAction(playerNumber, VIPER, GALACTICA_SPACE_6_OCLOCK),
                    new LaunchViperAction(playerNumber, ASSAULT_RAPTOR, GALACTICA_SPACE_4_OCLOCK),
                    new LaunchViperAction(playerNumber, ASSAULT_RAPTOR, GALACTICA_SPACE_6_OCLOCK)
            );
            default -> throw new FrakCallTheAdmiralException();
        };
    }
}
