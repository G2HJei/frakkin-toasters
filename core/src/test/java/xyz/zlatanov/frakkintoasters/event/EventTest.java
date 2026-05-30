package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.BeforeEach;
import xyz.zlatanov.frakkintoasters.fake.FakeDeck;
import xyz.zlatanov.frakkintoasters.fake.FakeDie;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.PegasusBoard;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;
import xyz.zlatanov.frakkintoasters.state.ship.*;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base test class for event-related tests.
 * <p>
 * This class provides a test harness that replaces production game components with fake/testable versions
 * to streamline testing of game events. It maintains references to all major game components including
 * boards, ships, and various decks of cards.
 * <p>
 * Test classes should extend this class and use {@link #setUpGame(Game)} to initialize the test environment
 * with a game template. All deck implementations and the die are automatically replaced with fake versions
 * that allow precise control over randomness during testing.
 *
 * @see xyz.zlatanov.frakkintoasters.fake.FakeDie
 * @see xyz.zlatanov.frakkintoasters.fake.FakeDeck
 */
public class EventTest {
    /**
     * the game under test
     */
    protected Game game;

    protected GalacticaBoard   galacticaBoard;
    protected PegasusBoard     pegasusBoard;
    protected CylonFleetBoard  cylonFleetBoard;
    protected CylonShips       cylonShips;
    protected Deck<QuorumCard> presidentHand;

    protected FakeDie                   die;
    protected FakeDeck<CivilianShip>    civilianShips;
    protected FakeDeck<GalacticaDamage> galacticaDamage;
    protected FakeDeck<PegasusDamage>   pegasusDamage;
    protected FakeDeck<BasestarDamage>  basestarDamageDeck;
    protected FakeDeck<DestinationCard> destinationDeck;
    protected FakeDeck<SkillCard>       politicsDeck;
    protected FakeDeck<SkillCard>       leadershipDeck;
    protected FakeDeck<SkillCard>       tacticsDeck;
    protected FakeDeck<SkillCard>       pilotingDeck;
    protected FakeDeck<SkillCard>       engineeringDeck;
    protected FakeDeck<SkillCard>       treacheryDeck;
    protected FakeDeck<SkillCard>       destinyDeck;
    protected FakeDeck<QuorumCard>      quorumDeck;
    protected FakeDeck<CrisisCard>      crisisDeck;
    protected FakeDeck<SuperCrisisCard> superCrisisDeck;
    protected FakeDeck<LoyaltyCard>     loyaltyDeck;
    protected FakeDeck<LoyaltyCard>     loyaltyNotCylonDeck;
    protected FakeDeck<MutinyCard>      mutinyDeck;


    /**
     * Sets up the game under test with default settings. All production components (decks, die) will be
     * replaced with fake/testable versions.
     * <p>
     * Equivalent to calling {@code setUpGame(Game.builder().build())}.
     *
     * @see #setUpGame(Game)
     */
    @BeforeEach
    protected void setUpGame() {
        setUpGame(Game.builder().build());
    }

    /**
     * Sets up the game under test. All production components (decks, die) will be replaced with fake/testable versions
     * while preserving the game state (players, boards, current player, etc.)
     *
     * @param template the game instance to use as a template for initializing the test game
     */
    protected void setUpGame(Game template) {
        this.galacticaBoard = template.boards().galactica();
        this.pegasusBoard = template.boards().pegasus();
        this.cylonFleetBoard = template.boards().cylonFleet();
        this.cylonShips = template.cylonShips();
        this.presidentHand = template.presidentHand();

        this.die = new FakeDie();
        this.civilianShips = new FakeDeck<>(template.decks().civilianShips());
        this.galacticaDamage = new FakeDeck<>(template.decks().galacticaDamage());
        this.pegasusDamage = new FakeDeck<>(template.decks().pegasusDamage());
        this.basestarDamageDeck = new FakeDeck<>(template.decks().basestarDamage());
        this.destinationDeck = new FakeDeck<>(template.decks().destination());
        this.politicsDeck = new FakeDeck<>(template.decks().politics());
        this.leadershipDeck = new FakeDeck<>(template.decks().leadership());
        this.tacticsDeck = new FakeDeck<>(template.decks().tactics());
        this.pilotingDeck = new FakeDeck<>(template.decks().piloting());
        this.engineeringDeck = new FakeDeck<>(template.decks().engineering());
        this.treacheryDeck = new FakeDeck<>(template.decks().treachery());
        this.destinyDeck = new FakeDeck<>(template.decks().destiny());
        this.quorumDeck = new FakeDeck<>(template.decks().quorum());
        this.crisisDeck = new FakeDeck<>(template.decks().crisis());
        this.superCrisisDeck = new FakeDeck<>(template.decks().superCrisis());
        this.loyaltyDeck = new FakeDeck<>(template.decks().loyalty());
        this.loyaltyNotCylonDeck = new FakeDeck<>(template.decks().loyaltyNotCylon());
        this.mutinyDeck = new FakeDeck<>(template.decks().mutiny());

        this.game = Game.builder(template.players().size())
                .currentPlayer(template.currentPlayer())
                .step(template.step())
                .objective(template.objective())
                .boards(template.boards())
                .cylonShips(template.cylonShips())
                .nukes(template.nukes())
                .president(template.president())
                .presidentHand(template.presidentHand())
                .admiral(template.admiral())
                .cag(template.cag())
                .die(die)
                .decks(DecksHolder.builder()
                        .civilianShips(civilianShips)
                        .galacticaDamage(galacticaDamage)
                        .pegasusDamage(pegasusDamage)
                        .basestarDamage(basestarDamageDeck)
                        .destination(destinationDeck)
                        .politics(politicsDeck)
                        .leadership(leadershipDeck)
                        .tactics(tacticsDeck)
                        .piloting(pilotingDeck)
                        .engineering(engineeringDeck)
                        .treachery(treacheryDeck)
                        .destiny(destinyDeck)
                        .quorum(quorumDeck)
                        .crisis(crisisDeck)
                        .superCrisis(superCrisisDeck)
                        .loyalty(loyaltyDeck)
                        .loyaltyNotCylon(loyaltyNotCylonDeck)
                        .mutiny(mutinyDeck)
                        .build())
                .build();
    }

    protected Basestar basestar() {
        return cylonShips.basestar().orElseThrow();
    }

    protected Raider raider() {
        return cylonShips.raider().orElseThrow();
    }

    protected HeavyRaider heavyRaider() {
        return cylonShips.heavyRaider().orElseThrow();
    }

    protected Centurion centurion() {
        return cylonShips.centurion().orElseThrow();
    }

    protected Viper viper() {
        return galacticaBoard.removeFromReserves(Viper.class);
    }

    protected AssaultRaptor assaultRaptor() {
        return galacticaBoard.removeFromReserves(AssaultRaptor.class);
    }

    protected CivilianShip civilianShip() {
        return civilianShips.draw();
    }

    protected Player player(int num) {
        return game.player(num);
    }

    protected Followup execute(Event event) {
        return event.execute(game);
    }

    protected boolean isValid(Event event) {
        return event.isValid(game);
    }

    protected void assertValid(Event event) {
        assertTrue(isValid(event), () -> "Expected event to be valid: " + event);
    }

    protected void assertInvalid(Event event) {
        assertFalse(isValid(event), () -> "Expected event to be invalid: " + event);
    }

    protected void executeAndAssertNoFollowup(Event event) {
        executeAndAssertFollowup(event, Followup.NONE);
    }

    protected void executeAndAssertFollowup(Event event, Followup expected) {
        assertEquals(expected, execute(event));
    }

    protected Followup executeValid(Event event) {
        assertValid(event);
        return execute(event);
    }

    protected void place(Location location, Ship... ships) {
        galacticaBoard.place(location, ships);
    }

    protected Basestar basestarAt(Location location) {
        var basestar = basestar();
        place(location, basestar);
        return basestar;
    }

    protected Raider raiderAt(Location location) {
        var raider = raider();
        place(location, raider);
        return raider;
    }

    protected HeavyRaider heavyRaiderAt(Location location) {
        var heavyRaider = heavyRaider();
        place(location, heavyRaider);
        return heavyRaider;
    }

    protected Viper viperAt(Location location) {
        var viper = viper();
        place(location, viper);
        return viper;
    }

    protected AssaultRaptor assaultRaptorAt(Location location) {
        var assaultRaptor = assaultRaptor();
        place(location, assaultRaptor);
        return assaultRaptor;
    }

    protected List<Raider> raidersAt(Location location, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> raiderAt(location))
                .toList();
    }

    protected List<Viper> vipersAt(Location location, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> viperAt(location))
                .toList();
    }

    protected List<Basestar> basestarsAt(Location location, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> basestarAt(location))
                .toList();
    }

    protected Player selectCharacter(int playerNumber, Character character) {
        var player = player(playerNumber);
        player.selectCharacter(character);
        return player;
    }

    protected Player selectCharacter(Character character) {
        return selectCharacter(1, character);
    }

    protected void moveTo(Location location, Character character) {
        game.moveTo(location, character);
    }
    

    private <T extends HumanFighter> T pilotAt(Character character, T ship, Location location) {
        ship.pilot(character);
        place(location, ship);
        return ship;
    }

    protected Player giveSkillCards(int playerNumber, SkillCard... cards) {
        var player = player(playerNumber);
        player.gainSkillCards(cards);
        return player;
    }

    protected Player giveMutinyCards(int playerNumber, MutinyCard... cards) {
        var player = player(playerNumber);
        Arrays.stream(cards).forEach(player.mutinyCards()::add);
        return player;
    }

    protected Player giveLoyaltyCards(int playerNumber, LoyaltyCard... cards) {
        var player = player(playerNumber);
        Arrays.stream(cards).forEach(player.loyaltyCards()::add);
        return player;
    }

    protected void assertSkillCards(int playerNumber, SkillCard... expected) {
        assertEquals(List.of(expected), player(playerNumber).skillCards().cards());
    }

    protected void assertNoSkillCards(int playerNumber) {
        assertTrue(player(playerNumber).skillCards().cards().isEmpty());
    }

    protected void nextRoll(int result) {
        die.nextRoll(result);
    }

    @SafeVarargs
    protected final <T> void nextCard(FakeDeck<T> deck, T... cards) {
        Arrays.stream(cards).forEach(deck::nextCard);
    }

    protected <T extends Ship> void assertShipCount(Location location, Class<T> shipClass, int expected) {
        assertEquals(expected, galacticaBoard.shipsIn(location, shipClass).size());
    }

    protected void assertNoShips(Location location) {
        assertTrue(galacticaBoard.shipsIn(location).isEmpty());
    }

    protected Character admiral() {
        return game.admiral();
    }

    protected Character cag() {
        return game.cag();
    }

    protected Character president() {
        return game.president();
    }

    protected Location locate(Character character) {
        return game.locate(character);
    }
}
