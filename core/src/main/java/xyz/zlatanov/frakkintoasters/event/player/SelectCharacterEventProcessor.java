package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.MUST_LAUNCH_IN_VIPER;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.DISREPUTABLE;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.SUPPORT;

public class SelectCharacterEventProcessor extends EventProcessor<SelectCharacterEvent> {

    // The alternate versions of rule A3.7.2.1. Boomer and Athena aren't versions of one character: both may be in the
    // same game (rule 6.2.4).
    private static final List<Character[]> VERSIONS = List.of(
            new Character[]{GAIUS_BALTAR, GAIUS_BALTAR_ALT},
            new Character[]{KARL_HELO_AGATHON, KARL_HELO_AGATHON_ALT},
            new Character[]{TOM_ZAREK, TOM_ZAREK_ALT},
            new Character[]{LEE_APOLLO_ADAMA, LEE_ADAMA});

    private Character selectedCharacter;

    @Override
    public void init() {
        selectedCharacter = event.selectedCharacter();
    }

    @Override
    public boolean isValid() {
        return player.character() == null && canBeSelected(game, selectedCharacter);
    }

    /**
     * @return whether a player without a character may select this one: at setup (rule A3.7.2), or after an execution,
     * when he "may choose any character without regard to character type" (rule 7.1)
     */
    public static boolean canBeSelected(GameView game, Character character) {
        if (character == null) {
            return false;
        }
        // An executed character "may no longer be used during the game", and neither may his other version.
        val unavailable = Stream.concat(
                        selectedCharacters(game).stream(),
                        game.removedComponents().items().stream().filter(Character.class::isInstance).map(Character.class::cast))
                .toList();
        if (unavailable.contains(character) || otherVersionUnavailable(unavailable, character)) {
            return false;
        }
        if (game.turn().number() > 0) {
            // The game is under way, so this replaces an executed character. Cylon Leaders are chosen at setup only.
            return character.type() != CYLON_LEADER;
        }
        return typeIsMostPlentiful(game, character) && respectsCylonLeaderRule(game, character);
    }

    @Override
    public Followup process() {
        ops.player(player.number()).selectCharacter(selectedCharacter);
        // Tom Zarek (alt.)'s "Disreputable": he starts the game holding a Mutiny Card, and draws it at once when he
        // replaces an executed character (rule A2.1.1.18).
        val mutinyCard = player.hasAbility(DISREPUTABLE) ? single(new DrawMutinyCardEvent(player.number())) : NONE;
        return allOf(startLocation(), mutinyCard);
    }

    private Followup startLocation() {
        val setup = PlaceInSetupLocationEventProcessor.startLocations(game, selectedCharacter);
        if (setup.size() == 1) {
            moveToSetup(setup.getFirst());
            return NONE;
        } else if (setup.size() > 1) {
            return multipleSetupOptionsFollowup(setup);
        } else {
            return specialSetupFollowup();
        }
    }

    private static boolean otherVersionUnavailable(List<Character> unavailable, Character character) {
        return VERSIONS.stream()
                .anyMatch(pair ->
                        (unavailable.contains(pair[0]) && character == pair[1]) ||
                                (unavailable.contains(pair[1]) && character == pair[0]));
    }

    private static boolean typeIsMostPlentiful(GameView game, Character character) {
        val excludedTypes = List.of(SUPPORT, CYLON_LEADER);
        val selectedType = character.type();
        if (excludedTypes.contains(selectedType)) {
            return true;
        }
        val currentlySelectedCharacters = selectedCharacters(game);
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

    // "Only one Cylon Leader may be chosen in any game. To play a seven player game, one player must play a Cylon Leader,
    // but Cylon Leaders may not be used in a three player game." (rule A3.7.2.2)
    private static boolean respectsCylonLeaderRule(GameView game, Character character) {
        val playerCount = game.players().size();
        val cylonLeaderSelected = selectedCharacters(game).stream()
                .map(Character::type)
                .anyMatch(t -> t == CYLON_LEADER);
        if (character.type() != CYLON_LEADER) {
            // The last player to choose in a 7 player game must be the Leader if nobody else is.
            val lastToChoose = selectedCharacters(game).size() == playerCount - 1;
            return playerCount < 7 || cylonLeaderSelected || !lastToChoose;
        }
        return playerCount > 3 && !cylonLeaderSelected;
    }

    private static List<Character> selectedCharacters(GameView game) {
        return game.players()
                .stream()
                .map(PlayerView::character)
                .filter(Objects::nonNull)
                .toList();
    }

    private void moveToSetup(Location setupLocation) {
        PlaceInSetupLocationEventProcessor.startAt(ops, selectedCharacter, setupLocation);
    }

    private Followup multipleSetupOptionsFollowup(List<Location> setup) {
        return one(setup.stream()
                .map(loc -> (Event) new PlaceInSetupLocationEvent(event.playerNumber(), loc))
                .toArray(Event[]::new));
    }

    private Followup specialSetupFollowup() {
        return switch (selectedCharacter) {
            case KARL_HELO_AGATHON -> NONE;
            case LEE_APOLLO_ADAMA -> launchApollo();
            default -> throw new FrakCallTheAdmiralException();
        };
    }

    // "Setup: Launch and pilot a viper". Selected after an execution with no viper in the "Reserves", he starts on the
    // "Hangar Deck" instead (rule A2.1.1.12).
    private Followup launchApollo() {
        if (galacticaBoard.reserves().stream().noneMatch(Viper.class::isInstance)) {
            ops.player(selectedCharacter).moveTo(HANGAR_DECK);
            return NONE;
        }
        return single(new PlayerDecisionEvent<>(event.playerNumber(), LaunchViperEvent.class, MUST_LAUNCH_IN_VIPER));
    }
}
