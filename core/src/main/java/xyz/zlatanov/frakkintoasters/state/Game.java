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
import xyz.zlatanov.frakkintoasters.state.ship.CylonShips;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheck;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.*;
import java.util.stream.IntStream;

import static xyz.zlatanov.frakkintoasters.state.GameStep.SETUP;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;

@Builder
@Getter
@Accessors(fluent = true)
public class Game implements GameView {
    // todo separate counters in own classes?
    // todo add turns
    @Builder.Default
    private ObjectiveCard        objective           = KOBOL;
    @Builder.Default
    private Map<Integer, Player> players             = Map.of(1, new Player(1), 2, new Player(2), 3, new Player(3));
    @Setter
    @Builder.Default
    private int                  currentPlayerNumber = 1;
    @Setter
    @Builder.Default
    private GameStep             step                = SETUP;
    @Builder.Default
    private Die                  die                 = new Die();
    @Builder.Default
    private BoardsHolder         boards              = new BoardsHolder();
    @Builder.Default
    private DecksHolder          decks               = DecksHolder.builder().build();
    private SkillCheck           activeSkillCheck;
    @Builder.Default
    private CylonShips           cylonShips          = CylonShips.builder().build();
    @Builder.Default
    private int                  nukes               = 2;
    @Setter
    private Character            president;
    @Builder.Default
    private Deck<QuorumCard>     presidentHand       = new Deck<>(false);
    @Setter
    private Character            admiral;
    @Setter
    private Character            cag;
    @Builder.Default
    private RemovedComponents    removedComponents   = new RemovedComponents();


    public static GameBuilder builder() {
        return builder(3);
    }

    public static GameBuilder builder(int numberOfPlayers) {
        val playersMap = new TreeMap<Integer, Player>();
        for (int i = 1; i <= numberOfPlayers; i++) {
            playersMap.put(i, new Player(i));
        }
        return new GameBuilder()
                .players(playersMap);
    }

    @Override
    public List<Player> players() {
        return new ArrayList<>(players.values());
    }

    @Override
    public Player player(int playerNumber) {
        return players.get(playerNumber);
    }

    @Override
    public Player currentPlayer() {
        return player(currentPlayerNumber);
    }

    public Game setupGalacticaBoard() {
        val galacticaBoard = boards.galactica();
        val raiders = IntStream.range(0, 4).mapToObj(i -> cylonShips.raider()).map(Optional::orElseThrow).map(Ship.class::cast).toList();
        boards.galactica()
                .place(GALACTICA_SPACE_4_OCLOCK, galacticaBoard.removeFromReserves(Viper.class).orElseThrow()) // reserves are never empty during setup
                .place(GALACTICA_SPACE_6_OCLOCK, galacticaBoard.removeFromReserves(Viper.class).orElseThrow()) // reserves are never empty during setup
                .place(GALACTICA_SPACE_2_OCLOCK, List.of(decks.civilianShips().draw(), decks.civilianShips().draw()))
                .place(GALACTICA_SPACE_8_OCLOCK, cylonShips.basestar().orElseThrow())
                .place(GALACTICA_SPACE_8_OCLOCK, raiders);
        return this;
    }

    @Override
    public Location locate(Character character) {
        val locations = boards.all().stream()
                .map(board -> board.locate(character))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        assert locations.size() == 1;
        return locations.getFirst();
    }

    public SkillCheck startSkillCheck(SkillCheckType type) {
        activeSkillCheck = new SkillCheck(type);
        return activeSkillCheck;
    }
}