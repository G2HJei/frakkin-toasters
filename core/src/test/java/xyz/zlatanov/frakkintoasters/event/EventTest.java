package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.fake.FakeDeck;
import xyz.zlatanov.frakkintoasters.fake.FakeDie;
import xyz.zlatanov.frakkintoasters.fake.NotTestableDie;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.board.PegasusBoard;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;
import xyz.zlatanov.frakkintoasters.state.ship.*;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

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
 * @see FakeDie
 * @see FakeDeck
 */
public class EventTest {
    /**
     * the game under test
     */
    public Game game;

    public GalacticaBoard   galacticaBoard;
    public PegasusBoard     pegasusBoard;
    public CylonFleetBoard  cylonFleetBoard;
    public CylonShips       cylonShips;
    public Deck<QuorumCard> presidentHand;

    public FakeDie                   die;
    public FakeDeck<CivilianShip>    civilianShips;
    public FakeDeck<GalacticaDamage> galacticaDamage;
    public FakeDeck<PegasusDamage>   pegasusDamage;
    public FakeDeck<BasestarDamage>  basestarDamage;
    public FakeDeck<DestinationCard> destinationDeck;
    public FakeDeck<SkillCard>       politicsDeck;
    public FakeDeck<SkillCard>       leadershipDeck;
    public FakeDeck<SkillCard>       tacticsDeck;
    public FakeDeck<SkillCard>       pilotingDeck;
    public FakeDeck<SkillCard>       engineeringDeck;
    public FakeDeck<SkillCard>       treacheryDeck;
    public FakeDeck<SkillCard>       destinyDeck;
    public FakeDeck<QuorumCard>      quorumDeck;
    public FakeDeck<CrisisCard>      crisisDeck;
    public FakeDeck<SuperCrisisCard> superCrisisDeck;
    public FakeDeck<LoyaltyCard>     loyaltyDeck;
    public FakeDeck<LoyaltyCard>     loyaltyNotCylonDeck;
    public FakeDeck<MutinyCard>      mutinyDeck;


    /**
     * Sets up the game under test with default settings. All production components (decks, die) will be
     * replaced with fake/testable versions.
     * <p>
     * Equivalent to calling {@code setUpGame(Game.builder().build())}.
     *
     * @see #setUpGame(Game)
     */
    public void setUpGame() {
        setUpGame(Game.builder().build());
    }

    /**
     * Sets up the game under test. All production components (decks, die) will be replaced with fake/testable versions
     * while preserving the game state (players, boards, current player, etc.)
     *
     * @param template the game instance to use as a template for initializing the test game
     */
    public void setUpGame(Game template) {
        this.galacticaBoard = template.boards().galactica();
        this.pegasusBoard = template.boards().pegasus();
        this.cylonFleetBoard = template.boards().cylonFleet();
        this.cylonShips = template.cylonShips();
        this.presidentHand = template.presidentHand();

        this.die = template.die() instanceof FakeDie ? (FakeDie) template.die() : new NotTestableDie();
        this.civilianShips = new FakeDeck<>(template.decks().civilianShips());
        this.galacticaDamage = new FakeDeck<>(template.decks().galacticaDamage());
        this.pegasusDamage = new FakeDeck<>(template.decks().pegasusDamage());
        this.basestarDamage = new FakeDeck<>(template.decks().basestarDamage());
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
                        .basestarDamage(basestarDamage)
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

    public Basestar basestar() {
        return cylonShips.basestar().orElseThrow();
    }

    public Raider raider() {
        return cylonShips.raider().orElseThrow();
    }

    public HeavyRaider heavyRaider() {
        return cylonShips.heavyRaider().orElseThrow();
    }

    public Centurion centurion() {
        return cylonShips.centurion().orElseThrow();
    }
}
