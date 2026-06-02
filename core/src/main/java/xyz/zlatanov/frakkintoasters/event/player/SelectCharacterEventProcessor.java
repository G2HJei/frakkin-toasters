package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.SUPPORT;

public class SelectCharacterEventProcessor extends EventProcessor<SelectCharacterEvent> {

    private Character selectedCharacter;

    @Override
    public void init() {
        selectedCharacter = event.selectedCharacter();
    }

    @Override
    public boolean isValid() {
        return !characterAlreadySelected(game)
                && !altAlreadySelected(game)
                && typeIsMostPlentiful(game)
                && respectsCylonLeaderRule(game);
    }

    @Override
    public Followup process() {
        player().character(selectedCharacter);
        val setup = selectedCharacter.setup();
        if (setup.length == 1) {
            moveToSetup(game);
            return Followup.NONE;
        } else if (setup.length > 1) {
            return multipleSetupOptionsFollowup();
        } else {
            return specialSetupFollowup();
        }
    }

    private boolean characterAlreadySelected(Game game) {
        return currentlySelectedCharacters(game)
                .stream()
                .anyMatch(c -> c == selectedCharacter);
    }

    private boolean altAlreadySelected(Game game) {
        val currentSelections = currentlySelectedCharacters(game);
        return Stream.of(
                        new xyz.zlatanov.frakkintoasters.state.character.Character[]{GAIUS_BALTAR, GAIUS_BALTAR_ALT},
                        new xyz.zlatanov.frakkintoasters.state.character.Character[]{KARL_HELO_AGATHON, KARL_HELO_AGATHON_ALT},
                        new xyz.zlatanov.frakkintoasters.state.character.Character[]{TOM_ZAREK, TOM_ZAREK_ALT},
                        new xyz.zlatanov.frakkintoasters.state.character.Character[]{SHARON_BOOMER_VALERII, SHARON_ATHENA_AGATHON},
                        new xyz.zlatanov.frakkintoasters.state.character.Character[]{LEE_APOLLO_ADAMA, LEE_ADAMA})
                .anyMatch(pair ->
                        (currentSelections.contains(pair[0]) && selectedCharacter == pair[1]) ||
                                (currentSelections.contains(pair[1]) && selectedCharacter == pair[0]));
    }

    private boolean typeIsMostPlentiful(Game game) {
        val excludedTypes = List.of(SUPPORT, CYLON_LEADER);
        val selectedType = selectedCharacter.type();
        if (excludedTypes.contains(selectedType)) {
            return true;
        }
        val currentlySelectedCharacters = currentlySelectedCharacters(game);
        val mostPlentiful = Arrays.stream(xyz.zlatanov.frakkintoasters.state.character.Character.values())
                .filter(c -> !currentlySelectedCharacters.contains(c))
                .map(xyz.zlatanov.frakkintoasters.state.character.Character::type)
                .filter(t -> !excludedTypes.contains(t))
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()))
                .entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(List.of());
        return mostPlentiful.contains(selectedType);
    }

    private boolean respectsCylonLeaderRule(Game game) {
        val playerCount = game.players().size();
        if (selectedCharacter.type() != CYLON_LEADER) {
            return playerCount < 7;
        }
        val cylonLeaderSelected = currentlySelectedCharacters(game).stream()
                .map(xyz.zlatanov.frakkintoasters.state.character.Character::type)
                .anyMatch(t -> t == CYLON_LEADER);
        return playerCount > 3 && !cylonLeaderSelected;
    }

    private List<Character> currentlySelectedCharacters(Game game) {
        return game.players()
                .stream()
                .map(Player::character)
                .filter(Objects::nonNull)
                .toList();
    }

    private void moveToSetup(Game game) {
        game.moveTo(selectedCharacter.setup()[0], selectedCharacter);
    }

    private Followup multipleSetupOptionsFollowup() {
        return one(Arrays.stream(selectedCharacter.setup())
                .map(loc -> (Event) new MoveEvent(event.playerNumber(), loc, null))
                .toArray(Event[]::new));
    }

    private Followup specialSetupFollowup() {
        return switch (selectedCharacter) {
            case KARL_HELO_AGATHON -> Followup.NONE;
            case LEE_APOLLO_ADAMA -> single(new PlayerDecisionEvent<>(event.playerNumber(), LaunchViperEvent.class));
            default -> throw new FrakCallTheAdmiralException();
        };
    }
}
