package xyz.zlatanov.frakkintoasters;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.board.BoardsHolder;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.*;

import java.util.*;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

@Getter
@Accessors(fluent = true)
public class Game {
    // todo inject boards, decks and counters for testing purposes?
    // todo separate decks and counters in own classes?
    // todo add current player, turns
    private final Map<Integer, Player> players;
    private final ObjectiveCard        objective;
    private final BoardsHolder         boards        = new BoardsHolder();
    private       DecksHolder          decks         = new DecksHolder();
    private       ShipsHolder          ships;
    private       int                  nukes;
    @Setter
    private       Character            president;
    private final List<QuorumCard>     presidentHand = new ArrayList<>();
    @Setter
    private       Character            admiral;
    @Setter
    private       Character            cag;

    public Game(ObjectiveCard objective, int numberOfPlayers) {
        this.objective = objective;
        val playersMap = new LinkedHashMap<Integer, Player>();
        for (int i = 1; i <= numberOfPlayers; i++) {
            playersMap.put(i, new Player());
        }
        players = Map.copyOf(playersMap);
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
                .orElseThrow(FrakCallTheAdmiralException::new);
    }

    public void moveTo(Location location, Character character) {
        boards.all().forEach(b -> b.remove(character));
        boards.all().stream()
                .filter(b -> b.locations().contains(location))
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new)
                .place(location, character);
    }
}
