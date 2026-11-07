package xyz.zlatanov.frakkintoasters.state;

import lombok.*;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.journal.Journal;
import xyz.zlatanov.frakkintoasters.state.board.BoardsHolder;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShips;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheck;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.*;

import static xyz.zlatanov.frakkintoasters.state.GameStep.SETUP;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;

@Builder
@Getter
//todo replace getters/setters with explicit methods for View interface usage to ensure no modification is possible
@Accessors(fluent = true)
public class Game implements GameView {
    // todo separate counters in own classes?
    // todo add turns
    @Builder.Default
    private ObjectiveCard        objective           = KOBOL;
    @Builder.Default
    private Map<Integer, Player> players             = Map.of(1, new Player(1), 2, new Player(2), 3, new Player(3));
    @Setter(onMethod_ = @Mutates)
    @Builder.Default
    private int                  currentPlayerNumber = 1;
    @Getter(AccessLevel.NONE)
    private Integer              actingPlayerNumber; // null means the current player is acting
    @Setter(onMethod_ = @Mutates)
    @Builder.Default
    private GameStep             step                = SETUP;
    @Setter(onMethod_ = @Mutates)
    @Builder.Default
    private int                  turn                = 0;
    @Builder.Default
    private Randomness           randomness          = new Randomness();
    @Builder.Default
    private Die                  die                 = new Die(); // own Randomness: a builder default can't see the other fields, so seed both for a deterministic game
    @Builder.Default
    private BoardsHolder         boards              = new BoardsHolder();
    @Builder.Default
    private DecksHolder          decks               = DecksHolder.builder().build();
    private SkillCheck           activeSkillCheck;
    @Builder.Default
    private CylonShips           cylonShips          = CylonShips.builder().build();
    @Builder.Default
    private int                  nukes               = 2;
    @Setter(onMethod_ = @Mutates)
    private Character            president;
    @Builder.Default
    private Deck<QuorumCard>     presidentHand       = new Deck<>(false);
    @Setter(onMethod_ = @Mutates)
    private Character            admiral;
    @Setter(onMethod_ = @Mutates)
    private Character            cag;
    @Builder.Default
    private RemovedComponents    removedComponents   = new RemovedComponents();
    @Builder.Default
    private Journal              journal             = new Journal();


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

    @Override
    public int actingPlayerNumber() {
        return actingPlayerNumber != null ? actingPlayerNumber : currentPlayerNumber;
    }

    @Override
    public int playerNumber(Character character) {
        return players.entrySet().stream()
                .filter(e -> e.getValue().character() == character)
                .mapToInt(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new);
    }

    /**
     * @param actingPlayerNumber the player acting out of turn, or {@code null} when the current player is acting
     */
    @Mutates
    public Game actingPlayerNumber(Integer actingPlayerNumber) {
        this.actingPlayerNumber = actingPlayerNumber;
        return this;
    }

    @Override
    public Player actingPlayer() {
        return player(actingPlayerNumber());
    }


    @Override
    public Optional<Location> locate(Character character) {
        val locations = boards.all().stream()
                .map(board -> board.locate(character))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        assert locations.size() <= 1;
        return locations.stream().findFirst();
    }

    @Mutates
    public SkillCheck startSkillCheck(SkillCheckType type) {
        activeSkillCheck = new SkillCheck(type);
        return activeSkillCheck;
    }
}