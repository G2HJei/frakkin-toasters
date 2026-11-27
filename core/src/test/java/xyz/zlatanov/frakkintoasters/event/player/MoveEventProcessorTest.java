package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.character.HereticDiscardEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.ship.ViperMarkVII;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAPRICA_SIX;
import static xyz.zlatanov.frakkintoasters.state.character.Character.DANNA_BIERS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEOBEN_CONOY;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.FLEET_MOBILIZATION;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.INBOUND_NUKES;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.ALL_HANDS_ON_DECK;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BAIT;
import static xyz.zlatanov.frakkintoasters.state.TurnLimit.MUTINY_CARD_FROM_TREACHERY;

class MoveEventProcessorTest extends EventTestHarness<MoveEvent> {

    SkillCard skillCard = new SkillCard(0, ALL_HANDS_ON_DECK);

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
    }

    @Test
    void shouldNotMoveCharacterWhoIsNotOnTheBoard() {
        assertInvalid(new MoveEvent(1, RESEARCH_LAB, null));
    }

    // The Movement step doesn't ask him, so only a client could send it.
    @Test
    void shouldNotMoveOutOfTheBrig() {
        moveTo(BRIG, KARA_STARBUCK_THRACE);
        assertInvalid(new MoveEvent(1, RESEARCH_LAB, null));
    }

    @Test
    void shouldMoveWithinSameShip() {
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
        execute(new MoveEvent(1, RESEARCH_LAB, null));
        assertEquals(RESEARCH_LAB, locate(KARA_STARBUCK_THRACE));
    }

    @Test
    void shouldDiscardToMoveBetweenShips() {
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(skillCard);

        execute(new MoveEvent(1, PRESIDENTS_OFFICE, skillCard));

        assertEquals(PRESIDENTS_OFFICE, locate(KARA_STARBUCK_THRACE));
        assertNoSkillCards(1);
        assertEquals(skillCard, leadershipDeck.lastDiscarded());
    }

    // Moving between ships is a discard the player chooses: "Bait" draws him a Mutiny Card, once a turn (rule 10.2.1).
    @Test
    void shouldDrawAMutinyCardForDiscardingBaitToMoveBetweenShips() {
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
        val bait = new SkillCard(0, BAIT);
        player(1).gainSkillCards(bait);

        execute(new MoveEvent(1, PRESIDENTS_OFFICE, bait));

        assertFollowup(new DrawMutinyCardEvent(1));
    }

    @Test
    void shouldDrawOnlyOneMutinyCardATurnForTreachery() {
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
        val bait = new SkillCard(0, BAIT);
        player(1).gainSkillCards(bait);
        ops.useTurnLimit(1, MUTINY_CARD_FROM_TREACHERY);

        execute(new MoveEvent(1, PRESIDENTS_OFFICE, bait));
    }

    @Test
    void shouldNotBeAbleToHazardousLocations() {
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
        assertInvalid(new MoveEvent(1, BRIG, null));
    }

    @Test
    void shouldNotMoveToADestroyedColonialOne() {
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(skillCard);
        ops.keepCrisisCardInPlay(SkillCheckType.BOMB_ON_COLONIAL_1);

        assertInvalid(new MoveEvent(1, PRESIDENTS_OFFICE, skillCard));
    }

    @Test
    void shouldNotMoveToADestroyedPegasus() {
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(skillCard);
        pegasusBoard.destroy();

        assertInvalid(new MoveEvent(1, ENGINE_ROOM, skillCard));
    }

    @Test
    void shouldNotAllowHumansMoveToCylonLocations() {
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
        assertInvalid(new MoveEvent(1, CAPRICA, skillCard));
    }

    @Test
    void shouldNotAllowMovingToSpace() {
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
        assertInvalid(new MoveEvent(1, GALACTICA_SPACE_7_OCLOCK, skillCard));
    }

    @Test
    void shouldMoveInSpaceWhilePiloting() {
        assaultRaptorAt(GALACTICA_SPACE_3_OCLOCK).pilot(KARA_STARBUCK_THRACE);
        execute(new MoveEvent(1, GALACTICA_SPACE_5_OCLOCK, null));
        assertEquals(GALACTICA_SPACE_5_OCLOCK, locate(KARA_STARBUCK_THRACE));
    }

    @Test
    void shouldLandWhilePiloting() {
        val karasViper = viperAt(GALACTICA_SPACE_3_OCLOCK).pilot(KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(skillCard);

        execute(new MoveEvent(1, PRESIDENTS_OFFICE, skillCard));

        assertEquals(PRESIDENTS_OFFICE, locate(KARA_STARBUCK_THRACE));
        assertTrue(galacticaBoard.reserves().contains(karasViper));
        assertFalse(galacticaBoard.shipsInSpace().containsKey(karasViper));
        assertNull(karasViper.pilot());
    }

    public static Stream<Arguments> adjacencyTests() {
        return Stream.of(
                argumentSet("viper to same space", new Viper(1), GALACTICA_SPACE_3_OCLOCK, false),
                argumentSet("viper to space 4", new Viper(2), GALACTICA_SPACE_5_OCLOCK, true),
                argumentSet("viper to space 6", new Viper(3), GALACTICA_SPACE_7_OCLOCK, false),
                argumentSet("viper to space 8", new Viper(4), GALACTICA_SPACE_9_OCLOCK, false),
                argumentSet("viper to space 10", new Viper(5), GALACTICA_SPACE_11_OCLOCK, false),
                argumentSet("viper to space 12", new Viper(6), GALACTICA_SPACE_1_OCLOCK, true),
                argumentSet("Assault raptor to same space", new AssaultRaptor(7), GALACTICA_SPACE_3_OCLOCK, false),
                argumentSet("Assault raptor to space 4", new AssaultRaptor(8), GALACTICA_SPACE_5_OCLOCK, true),
                argumentSet("Assault raptor to space 6", new AssaultRaptor(9), GALACTICA_SPACE_7_OCLOCK, false),
                argumentSet("Assault raptor to space 8", new AssaultRaptor(10), GALACTICA_SPACE_9_OCLOCK, false),
                argumentSet("Assault raptor to space 10", new AssaultRaptor(11), GALACTICA_SPACE_11_OCLOCK, false),
                argumentSet("Assault raptor to space 12", new AssaultRaptor(12), GALACTICA_SPACE_1_OCLOCK, true),
                argumentSet("Viper Mk7 to same space", new ViperMarkVII(13), GALACTICA_SPACE_3_OCLOCK, false),
                argumentSet("Viper Mk7 to space 4", new ViperMarkVII(14), GALACTICA_SPACE_5_OCLOCK, true),
                argumentSet("Viper Mk7 to space 6", new ViperMarkVII(15), GALACTICA_SPACE_7_OCLOCK, true),
                argumentSet("Viper Mk7 to space 8", new ViperMarkVII(16), GALACTICA_SPACE_9_OCLOCK, false),
                argumentSet("Viper Mk7 to space 10", new ViperMarkVII(17), GALACTICA_SPACE_11_OCLOCK, true),
                argumentSet("Viper Mk7 to space 12", new ViperMarkVII(18), GALACTICA_SPACE_1_OCLOCK, true)
        );
    }

    @ParameterizedTest
    @MethodSource("adjacencyTests")
    void shouldValidateMovementAdjacency(HumanFighter ship, Location destination, boolean isValid) {
        ship.pilot(KARA_STARBUCK_THRACE);
        place(GALACTICA_SPACE_3_OCLOCK, ship);
        val event = new MoveEvent(1, destination, null);
        if (isValid) {
            assertDoesNotThrow(() -> execute(event));
        } else {
            assertInvalid(event);
        }
    }

    // "Clouded": Leoben Conoy chooses no location where another character stands.
    @Test
    void cloudedKeepsLeobenFromAnOccupiedLocation() {
        player(2).character(LEOBEN_CONOY);
        player(3).character(CAPRICA_SIX);
        moveTo(HUMAN_FLEET, LEOBEN_CONOY);
        moveTo(CAPRICA, CAPRICA_SIX);

        assertInvalid(new MoveEvent(2, CAPRICA, null));
    }

    @Test
    void cloudedLeobenMovesToAnEmptyLocation() {
        player(2).character(LEOBEN_CONOY);
        player(3).character(CAPRICA_SIX);
        moveTo(HUMAN_FLEET, LEOBEN_CONOY);
        moveTo(CAPRICA, CAPRICA_SIX);

        execute(new MoveEvent(2, CYLON_FLEET, null));

        assertEquals(CYLON_FLEET, locate(LEOBEN_CONOY));
    }

    // "Heretic": D'Anna Biers leaves the "Resurrection Ship" by discarding a Super Crisis Card of her choice.
    @Test
    void hereticCostsDannaASuperCrisisCard() {
        player(2).character(DANNA_BIERS);
        moveTo(RESURRECTION_SHIP, DANNA_BIERS);
        player(2).superCrisisCards().addOnTop(INBOUND_NUKES, FLEET_MOBILIZATION);

        execute(new MoveEvent(2, CAPRICA, null));

        assertEquals(CAPRICA, locate(DANNA_BIERS));
        assertFollowup(one(new HereticDiscardEvent(2, INBOUND_NUKES), new HereticDiscardEvent(2, FLEET_MOBILIZATION)));
    }

    @Test
    void hereticKeepsDannaWithoutASuperCrisisCard() {
        player(2).character(DANNA_BIERS);
        moveTo(RESURRECTION_SHIP, DANNA_BIERS);

        assertInvalid(new MoveEvent(2, CAPRICA, null));
    }

    // The card has to be in his hand (F-2928df).
    @Test
    void shouldNotDiscardACardHeDoesNotHold() {
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);

        assertInvalid(new MoveEvent(1, PRESIDENTS_OFFICE, skillCard));
    }

    // A fighter flies around Galactica only (F-86ead5).
    @Test
    void shouldNotFlyToTheCylonFleetBoard() {
        viperAt(GALACTICA_SPACE_3_OCLOCK).pilot(KARA_STARBUCK_THRACE);

        assertInvalid(new MoveEvent(1, CYLON_FLEET_SPACE_1, null));
    }
}
