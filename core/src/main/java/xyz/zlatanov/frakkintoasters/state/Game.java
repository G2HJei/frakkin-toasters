package xyz.zlatanov.frakkintoasters.state;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.board.BoardsHolder;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.*;

import java.util.*;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;

@Builder
@Getter
@Accessors(fluent = true)
public class Game {
    // todo separate decks and counters in own classes?
    // todo add current playerNumber, turns
    @Builder.Default
    private Map<Integer, Player> players       = Map.of(1, new Player(), 2, new Player(), 3, new Player());
    @Builder.Default
    private Die                  die           = new Die();
    @Builder.Default
    private ObjectiveCard        objective     = KOBOL;
    @Builder.Default
    private BoardsHolder         boards        = new BoardsHolder();
    @Builder.Default
    private DecksHolder          decks         = DecksHolder.builder().build();
    @Builder.Default
    private CylonShips           ships         = CylonShips.builder().build();
    @Builder.Default
    private int                  nukes         = 2;
    @Setter
    private Character            president;
    @Builder.Default
    private Deck<QuorumCard>     presidentHand = new Deck<>();
    @Setter
    private Character            admiral;
    @Setter
    private Character            cag;

    public static GameBuilder builder() {
        return builder(3);
    }

    public static GameBuilder builder(int numberOfPlayers) {
        val playersMap = new TreeMap<Integer, Player>();
        for (int i = 1; i <= numberOfPlayers; i++) {
            playersMap.put(i, new Player());
        }
        return new GameBuilder()
                .players(playersMap);
    }

    public List<Player> players() {
        return new ArrayList<>(players.values());
    }

    public Player player(int playerNumber) {
        return players.get(playerNumber);
    }

    public void setupGalacticaBoard() {
        boards.galactica()
                //todo improve raptor - assault raptor handling
                .addToReserves(List.of(new Viper(), new Viper(), new Viper(), new Viper(), new Raptor(), new Raptor(), new Raptor(), new Raptor(), new AssaultRaptor()))
                .addToDamagedShips(List.of(new ViperMarkVII(), new ViperMarkVII(), new ViperMarkVII(), new ViperMarkVII()))
                .place(GALACTICA_SPACE_4_OCLOCK, new Viper())
                .place(GALACTICA_SPACE_6_OCLOCK, new Viper())
                .place(GALACTICA_SPACE_2_OCLOCK, List.of(decks.civilianShips().draw(), decks.civilianShips().draw()))
                .place(GALACTICA_SPACE_8_OCLOCK, ships.basestar())
                .place(GALACTICA_SPACE_8_OCLOCK, List.of(ships.raider(), ships.raider(), ships.raider(), ships.raider()));
    }

    public Location locate(Character character) {
        return boards.all().stream()
                .map(board -> board.locate(character))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public void moveTo(Location location, Character character) {
        assert !location.isSpaceLocation();
        boards.all().forEach(b -> b.remove(character));
        boards.all().stream()
                .filter(b -> b.locations().contains(location))
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new)
                .place(location, character);
    }


    public void moveTo(Location location, Ship ship) {
        assert LOCATION_AREAS.get("Galactica space").contains(location);
        boards.galactica()
                .remove(ship)
                .place(location, ship);
    }

    public void damage(Location location) {
        val board = LOCATION_AREAS.get("Galactica").contains(location)
                ? boards.galactica()
                : boards.pegasus();
        val affectedCharacters = board
                .damage(location)
                .charactersIn(location);
        if (!affectedCharacters.isEmpty()) {
            board.place(SICKBAY, affectedCharacters.toArray(new Character[0]));
        }
    }
}
