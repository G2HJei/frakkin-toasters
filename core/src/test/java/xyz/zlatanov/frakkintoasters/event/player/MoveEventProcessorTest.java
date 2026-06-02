package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.HumanFighter;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.ship.ViperMarkVII;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.ALL_HANDS_ON_DECK;

class MoveEventProcessorTest extends EventTestHarness<MoveEvent> {

    SkillCard skillCard = new SkillCard(0, ALL_HANDS_ON_DECK);

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
    }

    @Test
    void shouldMoveWithinSameShip() {
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
        executeAndAssertNoFollowup(new MoveEvent(1, RESEARCH_LAB, null));
        assertEquals(RESEARCH_LAB, locate(KARA_STARBUCK_THRACE));
    }

    @Test
    void shouldDiscardToMoveBetweenShips() {
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
        skillCards(1, skillCard);

        executeAndAssertNoFollowup(new MoveEvent(1, PRESIDENTS_OFFICE, skillCard));

        assertEquals(PRESIDENTS_OFFICE, locate(KARA_STARBUCK_THRACE));
        assertNoSkillCards(1);
        assertEquals(skillCard, leadershipDeck.lastDiscarded());
    }

    @Test
    void shouldNotBeAbleToHazardousLocations() {
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
        assertInvalid(new MoveEvent(1, BRIG, null));
    }

    @Test
    void shouldNotAllowHumansMoveToCylonLocations() {
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
        assertInvalid(new MoveEvent(1, CAPRICA, skillCard));
    }

    @Test
    void shouldNotAllowMovingToSpace() {
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
        assertInvalid(new MoveEvent(1, GALACTICA_SPACE_6_OCLOCK, skillCard));
    }

    @Test
    void shouldMoveInSpaceWhilePiloting() {
        assaultRaptorAt(GALACTICA_SPACE_2_OCLOCK).pilot(KARA_STARBUCK_THRACE);
        executeAndAssertNoFollowup(new MoveEvent(1, GALACTICA_SPACE_4_OCLOCK, null));
        assertEquals(GALACTICA_SPACE_4_OCLOCK, locate(KARA_STARBUCK_THRACE));
    }

    @Test
    void shouldLandWhilePiloting() {
        val karasViper = viperAt(GALACTICA_SPACE_2_OCLOCK).pilot(KARA_STARBUCK_THRACE);
        skillCards(1, skillCard);

        executeAndAssertNoFollowup(new MoveEvent(1, PRESIDENTS_OFFICE, skillCard));

        assertEquals(PRESIDENTS_OFFICE, locate(KARA_STARBUCK_THRACE));
        assertTrue(galacticaBoard.reserves().contains(karasViper));
        assertNull(karasViper.pilot());
    }

    public static Stream<Arguments> adjacencyTests() {
        return Stream.of(
                argumentSet("viper to same space", new Viper(1), GALACTICA_SPACE_2_OCLOCK, false),
                argumentSet("viper to space 4", new Viper(2), GALACTICA_SPACE_4_OCLOCK, true),
                argumentSet("viper to space 6", new Viper(3), GALACTICA_SPACE_6_OCLOCK, false),
                argumentSet("viper to space 8", new Viper(4), GALACTICA_SPACE_8_OCLOCK, false),
                argumentSet("viper to space 10", new Viper(5), GALACTICA_SPACE_10_OCLOCK, false),
                argumentSet("viper to space 12", new Viper(6), GALACTICA_SPACE_12_OCLOCK, true),
                argumentSet("Assault raptor to same space", new AssaultRaptor(7), GALACTICA_SPACE_2_OCLOCK, false),
                argumentSet("Assault raptor to space 4", new AssaultRaptor(8), GALACTICA_SPACE_4_OCLOCK, true),
                argumentSet("Assault raptor to space 6", new AssaultRaptor(9), GALACTICA_SPACE_6_OCLOCK, false),
                argumentSet("Assault raptor to space 8", new AssaultRaptor(10), GALACTICA_SPACE_8_OCLOCK, false),
                argumentSet("Assault raptor to space 10", new AssaultRaptor(11), GALACTICA_SPACE_10_OCLOCK, false),
                argumentSet("Assault raptor to space 12", new AssaultRaptor(12), GALACTICA_SPACE_12_OCLOCK, true),
                argumentSet("Viper Mk7 to same space", new ViperMarkVII(13), GALACTICA_SPACE_2_OCLOCK, false),
                argumentSet("Viper Mk7 to space 4", new ViperMarkVII(14), GALACTICA_SPACE_4_OCLOCK, true),
                argumentSet("Viper Mk7 to space 6", new ViperMarkVII(15), GALACTICA_SPACE_6_OCLOCK, true),
                argumentSet("Viper Mk7 to space 8", new ViperMarkVII(16), GALACTICA_SPACE_8_OCLOCK, false),
                argumentSet("Viper Mk7 to space 10", new ViperMarkVII(17), GALACTICA_SPACE_10_OCLOCK, true),
                argumentSet("Viper Mk7 to space 12", new ViperMarkVII(18), GALACTICA_SPACE_12_OCLOCK, true)
        );
    }

    @ParameterizedTest
    @MethodSource("adjacencyTests")
    void shouldValidateMovementAdjacency(HumanFighter ship, Location destination, boolean isValid) {
        ship.pilot(KARA_STARBUCK_THRACE);
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, ship);
        val event = new MoveEvent(1, destination, null);
        if (isValid) {
            assertDoesNotThrow(() -> execute(event));
        } else {
            assertInvalid(event);
        }
    }
}