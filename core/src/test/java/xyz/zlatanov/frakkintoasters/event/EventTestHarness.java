package xyz.zlatanov.frakkintoasters.event;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.fake.FakeDeck;
import xyz.zlatanov.frakkintoasters.fake.FakeDie;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.board.*;
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
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.ship.*;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.CYLON_FLEET_SPACE;

/**
 * Base test class for event-related tests.
 * <p>
 * This class provides a test harness that replaces production game components with fake/testable versions
 * to streamline testing of game events. It maintains references to all major game components including
 * boards, ships, and valious decks of cards.
 * <p>
 * Test classes should extend this class and use {@link #setUpGame(Game)} to initialize the test environment
 * with a game template. All deck implementations and the die are automatically replaced with fake versions
 * that allow precise control over randomness during testing.
 *
 * @see FakeDie
 * @see FakeDeck
 */
public abstract class EventTestHarness<E extends Event> {

    /**
     * the game under test
     */
    protected Game game;

    protected GalacticaBoard  galacticaBoard;
    protected PegasusBoard    pegasusBoard;
    protected CylonFleetBoard cylonFleetBoard;
    protected CylonShips      cylonShips;

    protected Deck<QuorumCard>          presidentHand;
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

    protected Followup followup;

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

    protected void setUpGame(int playerCount) {
        setUpGame(Game.builder(playerCount).build());
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

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private EventProcessor<E> eventProcessor() {
        val testClassName = getClass().getName();
        try {
            val processorClassName = testClassName.substring(0, testClassName.length() - "Test".length());
            val processorClass = Class.forName(processorClassName);
            return (EventProcessor<E>) processorClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Test class name does not match processor name/package", e);
        }
    }

    /* === UTILITY METHODS === */
    /*  General */
    protected void nextRoll(int result) {
        die.nextRoll(result);
    }

    protected void moveTo(Location location, Character character) {
        game.moveTo(location, character);
    }

    protected Location locate(Character character) {
        return game.locate(character);
    }

    protected Location locate(Ship ship) {
        return galacticaBoard.locate(ship);
    }

    /* Player */
    protected Player player(int playerNumber) {
        return game.player(playerNumber);
    }

    protected void assertSkillCards(int playerNumber, SkillCard... expected) {
        assertEquals(Set.of(expected), new HashSet<>(player(playerNumber).skillCards().cards()));
    }

    protected void assertNoSkillCards(int playerNumber) {
        assertEquals(0, player(playerNumber).skillCards().size());
    }

    /* Event execution */
    protected Followup execute(E event) {
        followup = eventProcessor().execute(game, event);
        return followup;
    }

    protected void assertNoFollowup() {
        assertEquals(Followup.NONE, followup);
    }

    protected void assertFollowup(Followup expected) {
        assertEquals(expected, followup);
    }

    protected void assertFollowup(Event expected) {
        assertEquals(single(expected), followup);
    }

    protected void assertInvalid(E event) {
        assertThrows(InvalidActionException.class, () -> execute(event));
    }

    /* Ships */
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
        return galacticaBoard.removeFromReserves(Viper.class).orElseThrow();
    }

    protected AssaultRaptor assaultRaptor() {
        return galacticaBoard.removeFromReserves(AssaultRaptor.class).orElseThrow();
    }

    protected CivilianShip civilianShip() {
        return civilianShips.draw();
    }

    protected void place(Location location, Ship... ships) {
        getSpaceLocationsBoard(location).place(location, ships);
    }

    protected Basestar basestarAt(Location location) {
        val basestar = basestar();
        place(location, basestar);
        return basestar;
    }

    protected Raider raiderAt(Location location) {
        val raider = raider();
        place(location, raider);
        return raider;
    }

    protected HeavyRaider heavyRaiderAt(Location location) {
        val heavyRaider = heavyRaider();
        place(location, heavyRaider);
        return heavyRaider;
    }

    protected Viper viperAt(Location location) {
        val viper = viper();
        place(location, viper);
        return viper;
    }

    protected AssaultRaptor assaultRaptorAt(Location location) {
        val assaultRaptor = assaultRaptor();
        place(location, assaultRaptor);
        return assaultRaptor;
    }

    protected void assertNoShips(Location location) {
        assertShipCount(location, 0);
    }

    protected void assertShipCount(Location location, int expected) {
        assertEquals(expected, getSpaceLocationsBoard(location).shipsIn(location).size());
    }

    protected <T extends Ship> void assertShipCount(Location location, Class<T> shipClass, int expected) {
        assertEquals(expected, getSpaceLocationsBoard(location).shipsIn(location, shipClass).size());
    }

    private SpaceLocationsBoard getSpaceLocationsBoard(Location location) {
        SpaceLocationsBoard board = CYLON_FLEET_SPACE.locations().contains(location)
                ? cylonFleetBoard
                : galacticaBoard;
        return board;
    }

    /* Titles */

    protected Character admiral() {
        return game.admiral();
    }

    protected Character cag() {
        return game.cag();
    }

    protected Character president() {
        return game.president();
    }

    protected void admiral(Character admiral) {
        game.admiral(admiral);
    }

    protected void cag(Character cag) {
        game.cag(cag);
    }

    protected void president(Character president) {
        game.president(president);
    }

}
