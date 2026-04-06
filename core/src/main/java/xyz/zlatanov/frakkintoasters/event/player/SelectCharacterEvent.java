package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
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

import static xyz.zlatanov.frakkintoasters.event.Followup.followWith;
import static xyz.zlatanov.frakkintoasters.event.Followup.oneOf;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.SUPPORT;

public record SelectCharacterEvent(int player, Character selectedCharacter) implements Event {

    @Override
    public boolean isValid(Game game) {
        return !characterAlreadySelected(game)
                && !altAlreadySelected(game)
                && typeIsMostPlentiful(game)
                && respectsCylonLeaderRule(game);
    }

    @Override
    public void apply(Game game) {
        game.player(player).selectCharacter(selectedCharacter);
    }

    @Override
    public List<Followup> followup(Game game) {
        val setup = selectedCharacter.setup();
        if (setup.length == 1) {
            moveToSetup(game);
            return null;
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
                        new Character[]{GAIUS_BALTAR, GAIUS_BALTAR_ALT},
                        new Character[]{KARL_HELO_AGATHON, KARL_HELO_AGATHON_ALT},
                        new Character[]{TOM_ZAREK, TOM_ZAREK_ALT},
                        new Character[]{SHARON_BOOMER_VALERII, SHARON_ATHENA_AGATHON},
                        new Character[]{LEE_APOLLO_ADAMA, LEE_ADAMA})
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
        val mostPlentiful = Arrays.stream(Character.values())
                .filter(c -> !currentlySelectedCharacters.contains(c))
                .map(Character::type)
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
                .map(Character::type)
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

    private List<Followup> multipleSetupOptionsFollowup() {

        return followWith(oneOf(
                Arrays.stream(selectedCharacter.setup())
                        .map(loc -> (Event) new MoveEvent(player, loc, null))
                        .toArray(Event[]::new)));
    }

    private List<Followup> specialSetupFollowup() {
        return switch (selectedCharacter) {
            case KARL_HELO_AGATHON -> null;
            case LEE_APOLLO_ADAMA -> followWith(new PlayerDecisionEvent(player, LaunchViperEvent.class));
            default -> throw new FrakCallTheAdmiralException();
        };
    }
}
